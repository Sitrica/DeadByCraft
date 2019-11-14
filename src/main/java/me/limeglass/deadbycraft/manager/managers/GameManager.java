package me.limeglass.deadbycraft.manager.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.abilities.Ability;
import me.limeglass.deadbycraft.database.Database;
import me.limeglass.deadbycraft.events.GameCreateEvent;
import me.limeglass.deadbycraft.events.GameEndEvent;
import me.limeglass.deadbycraft.events.GameStartEvent;
import me.limeglass.deadbycraft.events.PlayerJoinGameEvent;
import me.limeglass.deadbycraft.events.PlayerLeaveGameEvent;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;
import me.limeglass.deadbycraft.manager.managers.StructureManager.StructureInfo;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.State;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.objects.Generator;
import me.limeglass.deadbycraft.tasks.GameTask;
import me.limeglass.deadbycraft.tasks.LobbyTask;
import me.limeglass.deadbycraft.utils.IntervalUtils;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class GameManager extends Manager {

	private final Set<ArenaInfo> infos = new HashSet<>();
	private final Set<Game> lobbies = new HashSet<>();
	private final Set<Game> games = new HashSet<>();
	private Database<ArenaInfo> database;
	private final boolean bungeecord;
	private Game bungeeGame;

	public GameManager() {
		super(true);
		DeadByCraft instance = DeadByCraft.getInstance();
		FileConfiguration configuration = instance.getConfig();
		String table = configuration.getString("database.player-table", "Players");
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(table, ArenaInfo.class);
		else
			database = getFileDatabase(table, ArenaInfo.class);
		String interval = configuration.getString("database.autosave", "5 miniutes");
		Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> infos.forEach(info -> save(info)), 0, IntervalUtils.getInterval(interval));
		bungeecord = instance.getConfig().getBoolean("general.bungeecord", false);
		if (!bungeecord)
			return;
		bungeeGame = new Game();
		lobbies.add(bungeeGame);
		new LobbyTask(instance, bungeeGame).runTaskTimerAsynchronously(instance, 0, 20);
	}

	public void save(ArenaInfo info) {
		if (info.isSaving())
			return;
		database.put(info.getName(), info);
	}

	public boolean isMonster(Player player) {
		return games.stream()
				.map(game -> game.getMonster())
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.anyMatch(monster -> monster.getUniqueId().equals(player.getUniqueId()));
	}

	public boolean isBungeeMode() {
		return bungeecord;
	}

	public Game getBungeeGame() {
		return bungeeGame;
	}

	public Optional<ArenaInfo> getArenaInfo(String name) {
		return Optional.ofNullable(infos.parallelStream()
				.filter(info -> info.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElseGet(() -> {
					ArenaInfo info = database.get(name);
					if (info != null)
						infos.add(info);
					return info;
				}));
	}

	public boolean isGameRunning(String name) {
		return getRunningGame(name).isPresent();
	}

	public Set<Game> getRunningGames() {
		return games;
	}

	public Optional<Game> getRunningGame(String name) {
		Optional<Game> found = games.stream()
				.filter(game -> game.getName().equalsIgnoreCase(name))
				.findFirst();
		if (found.isPresent())
			return found;
		return lobbies.stream()
				.filter(lobby -> lobby.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	public Game getOrCreateGame(ArenaInfo info) {
		return getRunningGame(info.getName()).orElse(startLobby(info));
	}

	public Set<ArenaInfo> getArenaInfos() {
		return database.getKeys().parallelStream()
				.map(name -> getArenaInfo(name))
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	// Whatever is done within this method, should also be cloned into stopGame method.
	// This is because of recurrent.
	public void leaveGame(GamePlayer gamePlayer) {
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		gamePlayer.getPlayer().ifPresent(player -> {
			if (player.getGameMode() == GameMode.SPECTATOR)
				player.setSpectatorTarget(null);
			player.teleport(gamePlayer.getJoinLocation());
			player.setGameMode(GameMode.SURVIVAL);
		});
		PlayerLeaveGameEvent event = new PlayerLeaveGameEvent(game, gamePlayer);
		Bukkit.getPluginManager().callEvent(event);
		game.removePlayer(gamePlayer);
		gamePlayer.loadInventory();
		if (game.getPlayers().isEmpty())
			stopGame(game);
	}

	public void addArenaInfo(ArenaInfo info) {
		database.put(info.getName(), info);
		infos.add(info);
	}

	public void deleteArenaInfo(String name) {
		Iterator<ArenaInfo> iterator = infos.iterator();
		while (iterator.hasNext()) {
			ArenaInfo info = iterator.next();
			if (info.getName().equalsIgnoreCase(name)) {
				getRunningGame(name).ifPresent(game -> stopGame(game));
				iterator.remove();
				database.delete(name);
			}
		}
	}

	public void joinGame(GamePlayer gamePlayer, Game game) {
		if (!gamePlayer.isOnline())
			return;
		PlayerJoinGameEvent event = new PlayerJoinGameEvent(game, gamePlayer);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		game.addPlayer(gamePlayer);
		Player player = gamePlayer.getPlayer().get();
		gamePlayer.saveInventory();
		new MessageBuilder("lobby.joined")
				.replace("%player%", player.getName())
				.setPlaceholderObject(game)
				.send(game.getBukkitPlayers());
		player.getInventory().clear();
		player.setHealth(20.0);
		player.setFoodLevel(20);
		DeadByCraft.getInstance().getManager(GameMechanicManager.class).giveLobbyItems(gamePlayer);
		if (!bungeecord) {
			player.teleport(game.getArenaInfo().getLobby());
			return;
		}
		DeadByCraft.getInstance().getManager(DataManager.class).getGlobalSpawn().ifPresent(spawn -> player.teleport(spawn));
	}

	public void stopGame(Game game) {
		GameEndEvent event = new GameEndEvent(game);
		Bukkit.getPluginManager().callEvent(event);
		Set<GamePlayer> players = game.getPlayers();
		Iterator<GamePlayer> iterator = players.iterator();
		while (iterator.hasNext()) {
			GamePlayer gamePlayer = iterator.next();
			iterator.remove();
			game.removePlayer(gamePlayer);
			gamePlayer.getPlayer().ifPresent(player -> {
				if (player.getGameMode() == GameMode.SPECTATOR)
					player.setSpectatorTarget(null);
				player.teleport(gamePlayer.getJoinLocation());
				player.setGameMode(GameMode.SURVIVAL);
			});
		}
		lobbies.remove(game);
		games.remove(game);
		game.getArenaInfo().rebuild();
	}

	public Game startLobby(ArenaInfo info) {
		Game game = new Game(info);
		lobbies.add(game);
		GameCreateEvent event = new GameCreateEvent(game);
		Bukkit.getPluginManager().callEvent(event);
		DeadByCraft instance = DeadByCraft.getInstance();
		new LobbyTask(instance, game).runTaskTimerAsynchronously(instance, 0, 20);
		return game;
	}

	public void startGame(Game game) {
		GameStartEvent event = new GameStartEvent(game);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		lobbies.remove(game);
		if (game.getPlayers().size() < 2 || game.getState() != State.LOBBY) {
			stopGame(game);
			return;
		}

		// Setup the game to be in game mode.
		DeadByCraft instance = DeadByCraft.getInstance();
		ArenaInfo info = game.getArenaInfo();
		game.setState(State.GAME);
		games.add(game);
		info.rebuild();
		String generatorCompleteString = instance.getConfig().getString("game.generators.seconds", "1 minute and 20 seconds");
		long generatorCompleteTime = IntervalUtils.getInterval(generatorCompleteString) / 20;
		StructureManager structures = instance.getManager(StructureManager.class);
		StructureInfo generatorInfo = structures.getGenerator();
		info.getGenerators().stream()
				.map(location -> {
					structures.pasteStructure(generatorInfo, location);
					return new Generator(location, generatorCompleteTime);
				})
				.filter(generator -> generator != null)
				.forEach(generator -> game.addGenerator(generator));

		game.getBukkitPlayers().forEach(player -> player.getInventory().clear());

		// Determine roles.
		if (!game.getMonster().isPresent())
			game.determineMonster();
		if (instance.getConfig().getBoolean("general.announce-monster", false))
			new MessageBuilder("arenas.monster")
					.toPlayers(game.getBukkitPlayers())
					.setPlaceholderObject(game)
					.send();

		// Effects
		new MessageBuilder(false, "titles.monster")
				.setPlaceholderObject(game)
				.fromConfiguration(instance.getConfig())
				.sendTitle(game.getMonster().get().getPlayer().get());
		new MessageBuilder(false, "titles.survivor")
				.toPlayers(game.getBukkitSurvivors())
				.setPlaceholderObject(game)
				.fromConfiguration(instance.getConfig())
				.sendTitle();
		new SoundPlayer("game.start").playTo(game.getBukkitPlayers());
		if (instance.getConfig().getBoolean("game.blindness-on-start", true)) {
			PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, 140, 3);
			game.getBukkitPlayers().forEach(player -> player.addPotionEffect(effect));
		}

		CharacterManager characterManager = instance.getManager(CharacterManager.class);
		// Start abilities
		for (GamePlayer player : game.getPlayers()) {
			game.getSelectionOrAssign(player); //determine their character if they haven't picked.
			for (Ability ability : characterManager.getAbilitiesFor(player, game))
				ability.onGameStart(game, player);
		}

		for (GamePlayer gamePlayer : game.getPlayers()) {
			Optional<GameCharacter> character = game.getSelectionOrAssign(gamePlayer);
			if (!character.isPresent()) // Only happens when all characters are picked.
				continue;
			Optional<Player> player = gamePlayer.getPlayer();
			if (!player.isPresent())
				continue;
			character.get().getKit().stream().map(builder -> builder
						.replace("%player%", player.get().getName())
						.setPlaceholderObject(game)
						.build())
					.forEach(item -> player.get().getInventory().addItem(item));
		}

		// teleport to spawns
		List<Location> spawns = Lists.newArrayList(info.getSpawns());
		Collections.shuffle(spawns);
		int spot = 0;
		for (GamePlayer gamePlayer : game.getSurvivors()) {
			Optional<Player> optional = gamePlayer.getPlayer();
			if (!optional.isPresent())
				continue;
			Player player = optional.get();
			// Should only happen if insane amount of players or limited amount of spawns.
			if (spawns.size() >= spot) {
				spot = 0;
				Collections.shuffle(spawns);
			}
			Location location = spawns.get(spot);
			player.teleport(location);
			spot++;
		}
		// Teleport monster right after the 7 seconds of blindness are off.
		Bukkit.getScheduler().runTaskLater(instance, () -> {
			Optional<GamePlayer> player = game.getMonster();
			if (!player.isPresent())
				return; // Will be handled in the GameTask.
			Optional<Player> monster = player.get().getPlayer();
			if (!monster.isPresent())
				return; // Will be handled in the GameTask.
			monster.get().teleport(spawns.get(0));
		}, 139);

		// Run game system
		new GameTask(instance, game).runTaskTimer(instance, 0, 20);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		PlayerManager playerManager = DeadByCraft.getInstance().getManager(PlayerManager.class);
		leaveGame(playerManager.getGamePlayer(event.getPlayer()));
	}

}
