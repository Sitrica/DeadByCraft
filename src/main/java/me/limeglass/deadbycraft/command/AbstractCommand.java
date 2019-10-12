package me.limeglass.deadbycraft.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.utils.Formatting;

public abstract class AbstractCommand {

	private final String[] commands;
	private final boolean console;

	protected AbstractCommand(boolean console, String... commands) {
		this.console = console;
		this.commands = commands;
	}

	protected enum ReturnType {
		SUCCESS,
		FAILURE,
		SYNTAX_ERROR
	}

	public boolean containsCommand(String input) {
		for (String command : commands) {
			if (command.equalsIgnoreCase(input))
				return true;
		}
		return false;
	}

	protected boolean isConsoleAllowed() {
		return console;
	}

	protected String[] getCommands() {
		return commands;
	}

	protected abstract ReturnType runCommand(String command, CommandSender sender, String... arguments);

	public abstract String getConfigurationNode();

	public abstract String[] getPermissionNodes();

	public String getDescription(CommandSender sender) {
		FileConfiguration messages = DeadByCraft.getInstance().getConfiguration("messages").get();
		String description = messages.getString("commands." + getConfigurationNode() + ".description");
		return Formatting.color(description);
	}

	public String getSyntax(CommandSender sender) {
		FileConfiguration messages = DeadByCraft.getInstance().getConfiguration("messages").get();
		String syntax = messages.getString("commands." + getConfigurationNode() + ".syntax");
		return Formatting.color(syntax);
	}

	protected List<String> onTabComplete(Command command, CommandSender sender, String alias, String... arguments) {
		List<String> completions = new ArrayList<>();
		StringUtil.copyPartialMatches(arguments[0], Lists.newArrayList(commands), completions);
		Collections.sort(completions);
		return completions;
	}

}
