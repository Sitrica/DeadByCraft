package me.limeglass.deadbycraft.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.deadbycraft.DeadByCraft;

public class Formatting {

	public static String messagesPrefixed(ConfigurationSection section, String... nodes) {
		DeadByCraft instance = DeadByCraft.getInstance();
		FileConfiguration messages = instance.getConfiguration("messages").orElse(instance.getConfig());
		String complete = messages.getString("messages.prefix", "&7[&cDeadByCraft&7] &r");
		return Formatting.color(complete + messages(section, Arrays.copyOfRange(nodes, 0, nodes.length)));
	}

	public static String messages(ConfigurationSection section, String... nodes) {
		String complete = "";
		List<String> list = Arrays.asList(nodes);
		Collections.reverse(list);
		int i = 0;
		for (String node : list) {
			if (i == 0)
				complete = section.getString(node, "Error " + section.getCurrentPath() + "." + node) + complete;
			else
				complete = section.getString(node, "Error " + section.getCurrentPath() + "." + node) + " " + complete;
			i++;
		}
		return Formatting.color(complete);
	}

	public static String getPrefix() {
		DeadByCraft instance = DeadByCraft.getInstance();
		FileConfiguration messages = instance.getConfiguration("messages").orElse(instance.getConfig());
		return Formatting.color(messages.getString("messages.prefix", "&7[&cDeadByCraft&7] &r"));
	}

	public static String color(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

	public static String colorAndStrip(String input) {
		return stripColor(color(input));
	}

	public static String stripColor(String input) {
		return ChatColor.stripColor(input);
	}

}
