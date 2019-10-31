package me.limeglass.deadbycraft.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.abilities.Ability;
import me.limeglass.deadbycraft.manager.managers.ActionbarManager;
import me.limeglass.deadbycraft.manager.managers.CharacterManager;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;
import me.limeglass.deadbycraft.manager.managers.FinishManager;
import me.limeglass.deadbycraft.manager.managers.FinishManager.FinishReason;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.objects.Generator;
import me.limeglass.deadbycraft.utils.IntervalUtils;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;
import me.limeglass.deadbycraft.utils.Utils;

public class GameTask extends BukkitRunnable {

	private final long start = System.currentTimeMillis();
	private final CharacterManager characterManager;
	private final FileConfiguration sounds;
	private final GameManager gameManager;
	private final String timeout;
	private final double radius;
	private boolean gatesOpen;
	private final Game game;

	public GameTask(DeadByCraft instance, Game game) {
		this.timeout = instance.getConfig().getString("game.game-time", "45 minutes");
		this.radius = instance.getConfig().getDouble("game.generator.radius", 2.8);
		this.characterManager = instance.getManager(CharacterManager.class);
		this.gameManager = instance.getManager(GameManager.class);
		this.sounds = instance.getConfiguration("sounds").get();
		this.game = game;
	}

	@Override
	public void run() {
		Random random = new Random();
		if (System.currentTimeMillis() - start > IntervalUtils.getMilliseconds(timeout)) {
			cancel();
			//TODO have a finish game.
			return;
		}
		if (game.getPlayers().size() < 2) {
			if (game.getPlayers().size() == 0) {
				gameManager.stopGame(game);
				cancel();
				return;
			}
			DeadByCraft.getInstance().getManager(FinishManager.class).finishGame(game, FinishReason.LEAVE);
			new MessageBuilder("arenas.players-left")
					.setPlaceholderObject(game)
					.send(game.getBukkitPlayers());
			cancel();
			return;
		}

		// Generators Task.
		if (game.generatorsAreComplete() && !gatesOpen) {
			gatesOpen = true;
			//game.getArenaInfo().getGates().forEach(info -> info.getLocation().getBlock().setType(Material.AIR));
			new SoundPlayer("game.gates-ready").playTo(game.getBukkitPlayers());
			new MessageBuilder("titles.gates-ready")
					.toPlayers(game.getBukkitSurvivors())
					.setPlaceholderObject(game)
					.sendTitle();
		}
		for (Generator generator : game.getGenerators()) {
			Location origin = generator.getOrigin();
			if (generator.isRunning()) {
				if (sounds.getBoolean("generators.enabled", true))
					new SoundPlayer("generators.piston").playAt(origin);
				continue;
			}
			if (sounds.getBoolean("generators.enabled", true)) {
				int frequency = sounds.getInt("generators.frequency", 23);
				int max = Math.round(generator.getProgress() / frequency);
				for (int i = 0; i < Math.random() * max; i++) {
					Bukkit.getScheduler().runTaskLater(DeadByCraft.getInstance(), () -> {
						int rando = (int) (Math.random() * 3);
						String name = sounds.getString("generators.sound-" + rando + ".sound", "BLOCK_ANVIL_STEP");
						Sound sound = Utils.soundAttempt(name, "BLOCK_ANVIL_STEP");
						double volume = sounds.getDouble("generators.sound-" + rando + ".volume", 0.05);
						origin.getWorld().playSound(origin, sound, (float) volume, random.nextInt(8 - 0 + 1) + 0);
					}, i == 0 ? 0 : random.nextInt(20 - 1 + 1));
				}
			}
			Set<Player> players = generator.getNearbyPlayers(radius);
			for (int i = 0; i < players.size(); i++) {
				generator.increaseProgress();
			}
			if (generator.getProgress() > generator.getCompleteTime()) {
				generator.setRunning(true);
				Location location = generator.getOrigin();
				Firework firework = location.getWorld().spawn(location, Firework.class);
				FireworkEffect effect = FireworkEffect.builder()
						.withColor(Color.RED, Color.WHITE)
						.with(Type.BALL_LARGE)
						.withTrail()
						.build();
				FireworkMeta meta = firework.getFireworkMeta();
				meta.addEffect(effect);
				meta.addEffects(FireworkEffect.builder()
						.withColor(Color.RED)
						.with(Type.BALL)
						.withFlicker()
						.build());
				meta.setPower(1);
				firework.setFireworkMeta(meta);
				DeadByCraft.getInstance().getManager(ActionbarManager.class).sendActionBar(players, new MessageBuilder("arenas.generator-finished")
						.setPlaceholderObject(game)
						.get());
				continue;
			}
			new MessageBuilder(false, "titles.progress")
					.fromConfiguration(DeadByCraft.getInstance().getConfig())
					.setPlaceholderObject(generator)
					.toPlayers(players)
					.sendTitle();
		};

		// Experience gain every second
		for (GamePlayer player : game.getPlayers()) {
			game.addExperience(player, 1);
		}

		// Tick abilities
		for (GamePlayer player : game.getPlayers()) {
			Optional<GameCharacter> character = game.getSelectionOrAssign(player);
			if (!character.isPresent()) {
				new MessageBuilder("lobby.no-characters")
						.setPlaceholderObject(game)
						.send(game.getBukkitPlayers());
				gameManager.stopGame(game);
				continue;
			}
			for (Ability ability : characterManager.getAbilitiesFor(player, game)) {
				ability.tick(game, player);
			}
		}

		// Monster checking
		if (!game.getMonster().isPresent()) {
			new MessageBuilder("arenas.monster-left")
					.setPlaceholderObject(game)
					.send(game.getBukkitPlayers());
			game.determineMonster();
			Optional<GamePlayer> player = game.getMonster();
			if (!player.isPresent())
				return;
			Optional<Player> monster = player.get().getPlayer();
			if (!monster.isPresent())
				return;
			List<Location> spawns = Lists.newArrayList(game.getArenaInfo().getSpawns());
			Collections.shuffle(spawns);
			monster.get().teleport(spawns.get(0));
		} else if (DeadByCraft.getInstance().getConfig().getBoolean("game.monster-has-regeneration", false)) {
			Optional<Player> monster = game.getMonster().get().getPlayer();
			if (!monster.isPresent())
				return;
			PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, 20, 1);
			monster.get().addPotionEffect(effect);
		}
	}

}
