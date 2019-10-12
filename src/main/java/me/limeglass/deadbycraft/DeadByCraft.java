package me.limeglass.deadbycraft;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.minuskube.inv.InventoryManager;
import me.limeglass.deadbycraft.command.CommandHandler;
import me.limeglass.deadbycraft.manager.ExternalManager;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.manager.ManagerHandler;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.utils.Formatting;
import me.limeglass.glowing.GlowingAPI;

public class DeadByCraft extends JavaPlugin {

	private final Map<String, FileConfiguration> configurations = new HashMap<>();
	private final String packageName = "me.limeglass.deadbycraft";
	private static InventoryManager inventoryManager;
	private CommandHandler commandHandler;
	private ManagerHandler managerHandler;
	private static DeadByCraft instance;
	private GlowingAPI glowing;

	@Override
	public void onEnable() {
		instance = this;
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		if (!getDescription().getVersion().equals(getConfig().getString("version", getDescription().getVersion()))) {
			if (configFile.exists())
				configFile.delete();
		}
		//Create all the default files.
		for (String name : Arrays.asList("config", "messages", "sounds", "inventories", "data", "characters")) {
			File file = new File(getDataFolder(), name + ".yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				saveResource(file.getName(), false);
				debugMessage("Created new default file " + file.getName());
			}
			FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				configurations.put(name, configuration);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		glowing = new GlowingAPI(this);
		managerHandler = new ManagerHandler(this);
		commandHandler = new CommandHandler(this);
		inventoryManager = new InventoryManager(this);
		inventoryManager.init();
		DeadByCraftAPI.setInstance(this);
		consoleMessage("DeadByCraft has been enabled!");
	}

	public void onDisable() {
		GameManager gameManager = getManager(GameManager.class);
		gameManager.getRunningGames().forEach(game -> gameManager.stopGame(game));
	}

	public <T extends ExternalManager> T getExternalManager(Class<T> expected) {
		return managerHandler.getExternalManager(expected);
	}

	public static InventoryManager getInventoryManager() {
		return inventoryManager;
	}

	/**
	 * Grab a FileConfiguration if found.
	 * Call it without it's file extension, just the simple name of the file.
	 * 
	 * @param configuration The name of the configuration to search for.
	 * @return Optional<FileConfiguration> as the file may or may not exist.
	 */
	public Optional<FileConfiguration> getConfiguration(String configuration) {
		return Optional.ofNullable(configurations.get(configuration));
	}

	/**
	 * Grab a Manager by it's class and create it if not present.
	 * 
	 * @param <T> <T extends Manager>
	 * @param expected The expected Class that extends Manager.
	 * @return The Manager that matches the defined class.
	 */
	public <T extends Manager> T getManager(Class<T> expected) {
		return managerHandler.getManager(expected);
	}

	public GlowingAPI getGlowingAPI() {
		return glowing;
	}

	public static void consoleMessage(String string) {
		Bukkit.getConsoleSender().sendMessage(Formatting.color("&c[DeadByCraft]&7 " + string));
	}

	public static void debugMessage(String string) {
		if (instance.getConfig().getBoolean("debug"))
			consoleMessage("&b" + string);
	}

	/**
	 * @return The CommandManager allocated to the Kingdoms instance.
	 */
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public ManagerHandler getManagerHandler() {
		return managerHandler;
	}

	public static DeadByCraft getInstance() {
		return instance;
	}

	public List<Manager> getManagers() {
		return managerHandler.getManagers();
	}

	public String getPackageName() {
		return packageName;
	}

}
