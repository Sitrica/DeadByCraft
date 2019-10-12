package me.limeglass.deadbycraft.manager.managers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eclipse.jdt.annotation.Nullable;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.abilities.Ability;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.Role;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;
import me.limeglass.deadbycraft.utils.Utils;

public class CharacterManager extends Manager {

	private final Map<Ability, FileConfiguration> configurations = new HashMap<>();
	private final Map<Integer, Integer> unlocks = new HashMap<>();
	private final Set<GameCharacter> characters = new HashSet<>();
	private final Set<Ability> abilities = new HashSet<>();
	private final File FOLDER;

	public CharacterManager() {
		super(true);
		DeadByCraft instance = DeadByCraft.getInstance();
		FOLDER = new File(instance.getDataFolder() + "/abilities");
		if (!FOLDER.exists())
			FOLDER.mkdirs();
		for (Class<? extends Ability> clazz : Utils.getClassesOf(instance, instance.getPackageName() + ".abilities", Ability.class)) {
			if (clazz == Ability.class)
				continue;
			try {
				abilities.add(clazz.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				DeadByCraft.consoleMessage("&dAbility " + clazz.getName() + " doesn't have a nullable constructor.");
				e.printStackTrace();
				continue;
			}
		}
		// Load characters
		ConfigurationSection characterSection = instance.getConfiguration("characters").get().getConfigurationSection("characters");
		for (String node : characterSection.getKeys(false))
			characters.add(new GameCharacter(node, characterSection.getConfigurationSection(node)));

		// Load configurations for abilities.
		abilities.forEach(ability -> getAbilityConfiguration(ability));

		FileConfiguration configuration = instance.getConfig();
		if (!configuration.isConfigurationSection("abilities.ability-slots")) {
			unlocks.put(1, 0);
			unlocks.put(2, 5);
			unlocks.put(3, 10);
			unlocks.put(4, 15);
			unlocks.put(5, 20);
			return;
		} else {
			ConfigurationSection section = configuration.getConfigurationSection("abilities.ability-slots");
			for (String node : section.getKeys(false)) {
				int slot;
				try {
					slot = Integer.parseInt(node);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					continue;
				}
				int level = section.getInt(node, 5);
				unlocks.put(slot, level);
			}
		}
	}

	public Set<GameCharacter> getCharactersByRole(Role role) {
		return characters.stream()
				.filter(character -> character.getRole() == role)
				.collect(Collectors.toSet());
	}

	public Optional<GameCharacter> getCharacter(String name) {
		return characters.stream()
				.filter(character -> character.getNode().equalsIgnoreCase(name))
				.findFirst();
	}

	public Optional<Ability> getAbility(String name) {
		return abilities.stream()
				.filter(ability -> ability.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	public Collection<Ability> getAbilitiesFor(GamePlayer player, Game game) {
		Set<Ability> playerAbilities = new HashSet<>();
		Optional<GameCharacter> selection = game.getSelection(player);
		if (!selection.isPresent())
			return playerAbilities;
		return player.getCharacterLoadout(selection.get()).getAbilities().values();
	}

	public Set<Ability> getAbilitiesFor(GameCharacter character) {
		return abilities.stream()
				.filter(ability -> {
					AbilityConfiguration configuration = getAbilityConfiguration(ability);
					Set<GameCharacter> allowed = configuration.getAllowedCharacters();
					if (allowed.isEmpty())
						return true;
					return allowed.stream()
							.anyMatch(allow -> allow.getNode().equalsIgnoreCase(character.getNode()));
				})
				.filter(ability -> ability.getRole() == character.getRole())
				.collect(Collectors.toSet());
	}

	@Nullable
	public AbilityConfiguration getAbilityConfiguration(Ability ability) {
		FileConfiguration abilityConfiguration = configurations.entrySet().stream()
				.filter(entry -> entry.getKey().equals(ability))
				.map(entry -> entry.getValue())
				.findFirst()
				.orElseGet(() -> {
					File file = new File(FOLDER, ability.getName().toLowerCase(Locale.US) + ".yml");
					if (!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}
					FileConfiguration configuration = new YamlConfiguration();
					try {
						configuration.load(file);
					} catch (IOException | InvalidConfigurationException e) {}
					initalizeConfiguration(configuration, ability);
					try {
						configuration.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
					configurations.put(ability, configuration);
					return configuration;
				});
		if (abilityConfiguration == null)
			return null;
		return new AbilityConfiguration(abilityConfiguration);
	}

	public int getSlotLevel(int slot) {
		slot = slot + 1;
		if (slot < 1 || slot > 5)
			return -1;
		for (Entry<Integer, Integer> entry : unlocks.entrySet()) {
			if (entry.getKey() == slot)
				return entry.getValue();
		}
		return -1;
	}

	public boolean isSlotUnlocked(int slot, GamePlayer player) {
		slot = slot + 1;
		if (slot < 1 || slot > 5)
			return false;
		for (Entry<Integer, Integer> entry : unlocks.entrySet()) {
			if (entry.getKey() == slot)
				return player.getLevel() >= entry.getValue();
		}
		return false;
	}

	private void initalizeConfiguration(FileConfiguration configuration, Ability ability) {
		if (!configuration.isSet("enabled"))
			configuration.set("enabled", true);
		if (!configuration.isSet("name"))
			configuration.set("name", ability.getName());
		if (!configuration.isSet("icon.title"))
			configuration.set("icon.title", "&6&l" + ability.getName());
		if (!configuration.isSet("icon.material"))
			configuration.set("icon.material", ability.getMaterial().name());
		if (!configuration.isSet("icon.lore"))
			configuration.set("icon.lore", ability.getDescription());
		ability.initalizeConfiguration(configuration);
	}

	public class AbilityConfiguration {

		private final Set<String> allowed = new HashSet<>();
		private final FileConfiguration configuration;
		private final ItemStackBuilder icon;
		private final boolean enabled;
		private final String name;

		public AbilityConfiguration(FileConfiguration configuration) {
			this.configuration = configuration;
			if (configuration.isList("allowed-characters"))
				allowed.addAll(configuration.getStringList("allowed-characters"));
			enabled = configuration.getBoolean("enabled", true);
			name = configuration.getString("name", "Not named");
			icon = new ItemStackBuilder("icon")
					.replace("%characters%", allowed, character -> character)
					.replace("%character%", allowed, character -> character)
					.fromConfiguration(configuration)
					.replace("%name%", name);
		}

		public Set<GameCharacter> getAllowedCharacters() {
			return allowed.stream().map(name -> getCharacter(name))
					.filter(optional -> optional.isPresent())
					.map(optional -> optional.get())
					.collect(Collectors.toSet());
		}

		public FileConfiguration getConfiguration() {
			return configuration;
		}

		public ItemStackBuilder getIcon() {
			return icon;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public String getName() {
			return name;
		}

	}

	public class GameCharacter {

		private final Set<ItemStackBuilder> kit = new HashSet<>();
		private final ConfigurationSection section;
		private Role role = Role.SURVIVOR;
		private final String title, node;

		public GameCharacter(String node, ConfigurationSection section) {
			try {
				role = Role.valueOf(Role.class, section.getString("role", "SURVIVOR"));
			} catch (Exception e) {}
			if (section.isConfigurationSection("kit")) {
				ConfigurationSection kitSection = section.getConfigurationSection("kit");
				for (String name : kitSection.getKeys(false)) {
					kit.add(new ItemStackBuilder(kitSection.getConfigurationSection(name)));
				}
			}
			this.title = section.getString("title", node);
			this.section = section;
			this.node = node;
		}

		public Set<ItemStackBuilder> getKit() {
			return kit;
		}

		public boolean hasKit() {
			return !kit.isEmpty();
		}

		public String getTitle() {
			return title;
		}

		public String getNode() {
			return node;
		}

		public Role getRole() {
			return role;
		}

		public SoundPlayer getEquipSound() {
			return new SoundPlayer(section.getConfigurationSection("equip-sound"));
		}

		public ItemStackBuilder getIcon() {
			return new ItemStackBuilder(section);
		}

	}

}
