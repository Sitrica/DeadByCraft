package me.limeglass.deadbycraft.listener;

import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.FinishManager;
import me.limeglass.deadbycraft.manager.managers.FinishManager.FinishReason;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.objects.Game.State;

public class EventListener implements Listener {

	private final FileConfiguration configuration;
	private final PlayerManager playerManager;

	public EventListener(DeadByCraft instance) {
		this.playerManager = instance.getManager(PlayerManager.class);
		this.configuration = instance.getConfig();
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		if (optional.get().getState() != State.GAME)
			event.setCancelled(true);
	}

	/**
	 * Check if the player walks into a gate.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		if (game.getState() != State.GAME)
			return;
		if (!game.generatorsAreComplete())
			return;
		Location location = player.getLocation();
		if (!game.getArenaInfo().getGates().stream().anyMatch(info -> info.getLocation().distance(location) < 1))
			return;
		Optional<GamePlayer> monster = game.getMonster();
		if (!monster.isPresent())
			return;
		if (monster.get().equals(gamePlayer))
			return;
		new MessageBuilder("arenas.player-escaped")
				.replace("%total%", game.getSurvivors().size())
				.replace("%player%", player.getName())
				.setPlaceholderObject(game)
				.send(game.getBukkitPlayers());
		game.addEscapee(gamePlayer);
		player.setGameMode(GameMode.SPECTATOR);
		Optional<GamePlayer> randomSurvivor = game.getSurvivors().stream()
				.filter(survivor -> !survivor.equals(gamePlayer))
				.filter(survivor -> !game.hasEscaped(survivor))
				.findAny();
		DeadByCraft instance = DeadByCraft.getInstance();
		new MessageBuilder("titles.escaped-spectate")
				.fromConfiguration(instance.getConfig())
				.setPlaceholderObject(game)
				.send(game.getBukkitPlayers());
		if (!randomSurvivor.isPresent() || game.haveSurvivorsEscaped()) {
			instance.getManager(FinishManager.class).finishGame(game, FinishReason.ESCAPE);
			return;
		}
		player.setSpectatorTarget(randomSurvivor.get().getPlayer().get());
	}

	/**
	 * Stop spectators from using the vanilla teleport.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onSpectatorTeleport(PlayerTeleportEvent event) {
		if (event.getCause() != TeleportCause.SPECTATE)
			return;
		Player player = event.getPlayer();
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		if (game.getState() != State.GAME)
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		GamePlayer victim = playerManager.getGamePlayer(player);
		Optional<Game> optional = victim.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		if (optional.get().getState() != State.GAME) {
			event.setCancelled(true);
			return;
		}
		if (configuration.getBoolean("game.disable-fall-damage", true) && event.getCause() == DamageCause.FALL)
			event.setCancelled(true);
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
			Entity attacker = entityEvent.getDamager();
			if (attacker.getType() != EntityType.PLAYER)
				return;
			GamePlayer gameAttacker = playerManager.getGamePlayer((Player) attacker);
			Optional<Game> attackerGame = gameAttacker.getCurrentGame();
			if (!attackerGame.isPresent())
				return;
			if (!game.equals(attackerGame.get()))
				return;
			Optional<GamePlayer> monster = game.getMonster();
			if (monster.isPresent()) {
				if (monster.get().getUniqueId().equals(gameAttacker.getUniqueId()))
					return;
				if (DeadByCraft.getInstance().getConfig().getBoolean("game.allow-survivors-to-attack-monster", false)) {
					if (monster.get().equals(victim))
						return;
				}
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent event) {
		Player player = (Player) event.getEntity();
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		event.setFoodLevel(20);
	}

	@EventHandler(ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getType() != InventoryType.PLAYER)
			return;
		Player player = (Player) event.getWhoClicked();
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> game = gamePlayer.getCurrentGame();
		if (!game.isPresent())
			return;
		if (game.get().getState() != State.GAME)
			event.setCancelled(true);
	}

	//Events below are used when the game takes over the server. (Bungee mode)

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		GameManager gameManager = DeadByCraft.getInstance().getManager(GameManager.class);
		if (!gameManager.isBungeeMode())
			return;
		GamePlayer gamePlayer = playerManager.getGamePlayer(event.getPlayer());
		gameManager.joinGame(gamePlayer, gameManager.getBungeeGame());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		GameManager gameManager = DeadByCraft.getInstance().getManager(GameManager.class);
		if (!gameManager.isBungeeMode())
			return;
		GamePlayer gamePlayer = playerManager.getGamePlayer(event.getPlayer());
		gameManager.leaveGame(gamePlayer);
	}

}
