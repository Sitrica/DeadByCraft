package me.limeglass.deadbycraft.placeholders;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;
import me.limeglass.deadbycraft.manager.managers.GeneratorManager;
import me.limeglass.deadbycraft.manager.managers.SetupManager.Setup;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.objects.Generator;
import me.limeglass.deadbycraft.objects.StringList;

public class DefaultPlaceholders {

	public static void initalize() {
		DeadByCraft instance = DeadByCraft.getInstance();
		Placeholders.registerPlaceholder(new Placeholder<CommandSender>("%sender%") {
			@Override
			public String replace(CommandSender sender) {
				return sender.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%position1%") {
			@Override
			public String replace(Setup setup) {
				Optional<Location> pos1 = setup.getLocation("pos1");
				if (!pos1.isPresent())
					return null;
				Location location = pos1.get();
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%position2%") {
			@Override
			public String replace(Setup setup) {
				Optional<Location> pos2 = setup.getLocation("pos2");
				if (!pos2.isPresent())
					return null;
				Location location = pos2.get();
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%lobby%") {
			@Override
			public String replace(Setup setup) {
				Optional<Location> pos2 = setup.getLocation("lobby");
				if (!pos2.isPresent())
					return null;
				Location location = pos2.get();
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Game>("%min%") {
			@Override
			public String replace(Game game) {
				return game.getArenaInfo().getMinimumPlayers() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Game>("%escapees%") {
			@Override
			public String replace(Game game) {
				return game.getEscapees().size() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Game>("%max%") {
			@Override
			public String replace(Game game) {
				return game.getArenaInfo().getMaximumPlayers() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Game>("%players%") {
			@Override
			public String replace(Game game) {
				String[] players = game.getBukkitPlayers().stream()
						.map(player -> player.getName())
						.toArray(size -> new String[size]);
				return new StringList(players).toString();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Game>("%playercount%") {
			@Override
			public String replace(Game game) {
				return game.getPlayers().size() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%teleport%") {
			@Override
			public String replace(Setup setup) {
				Optional<Location> teleport = setup.getLocation("teleport");
				if (!teleport.isPresent())
					return null;
				Location location = teleport.get();
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%player%") {
			@Override
			public String replace(Setup setup) {
				Player player = setup.getPlayer();
				if (player == null)
					return null;
				return player.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%name%") {
			@Override
			public String replace(Setup setup) {
				String name = setup.getName();
				if (name == null)
					return null;
				return name;
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Player>("%player%") {
			@Override
			public String replace(Player player) {
				return player.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<String>("%string%") {
			@Override
			public String replace(String string) {
				return string;
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Generator>("%progress%", "%percentage%", "%percent%") {
			@Override
			public String replace(Generator generator) {
				return instance.getManager(GeneratorManager.class).getProgress(generator) + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Generator>("%progressbar%", "%progress-bar%") {
			@Override
			public String replace(Generator generator) {
				return instance.getManager(GeneratorManager.class).getProgressBar(generator);
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<GamePlayer>("%character%") {
			@Override
			public String replace(GamePlayer gamePlayer) {
				Optional<Game> game = gamePlayer.getCurrentGame();
				if (!game.isPresent())
					return null;
				Optional<GameCharacter> selection = game.get().getSelection(gamePlayer);
				return selection.isPresent() ? selection.get().getNode() : null;
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<GamePlayer>("%level%") {
			@Override
			public String replace(GamePlayer gamePlayer) {
				return gamePlayer.getLevel() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<GamePlayer>("%xp%", "%experience%") {
			@Override
			public String replace(GamePlayer gamePlayer) {
				return gamePlayer.getExperience() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Generator>("%color%", "%colour%") {
			@Override
			public String replace(Generator generator) {
				long progress = Math.round(instance.getManager(GeneratorManager.class).getProgress(generator));
				ConfigurationSection section = instance.getConfig().getConfigurationSection("game.generators.progress-colours");
				for (String node : section.getKeys(false)) {
					if (!node.contains("-")) {
						int number = number(node);
						if (number < 0)
							continue;
						if (progress != number)
							continue;
						String replacement = section.getString(node, "&f");
						return ChatColor.translateAlternateColorCodes('&', replacement);
					}
					String[] split = node.split("-");
					int start = number(split[0]);
					int end = number(split[1]);
					if (start < 0 || end < 0 || start > 100 || end > 100)
						continue;
					if (start < progress && progress < end) {
						String replacement = section.getString(node, "&f");
						return ChatColor.translateAlternateColorCodes('&', replacement);
					}
				}
				return ChatColor.translateAlternateColorCodes('&', "&f");
			}
		});
	}

	private static int number(String string) {
		try {
			return Integer.valueOf(string);
		} catch (Exception e) {
			return -1;
		}
	}

}
