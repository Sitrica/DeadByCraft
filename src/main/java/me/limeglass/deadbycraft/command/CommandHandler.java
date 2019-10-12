package me.limeglass.deadbycraft.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AbstractCommand.ReturnType;
import me.limeglass.deadbycraft.command.commands.DeadByCraftCommand;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;
import me.limeglass.deadbycraft.utils.Utils;

public class CommandHandler implements CommandExecutor, TabCompleter {

	private final List<AbstractCommand> commands = new ArrayList<>();

	public CommandHandler(DeadByCraft instance) {
		PluginCommand command = instance.getCommand("deadbycraft");
		command.setTabCompleter(this);
		command.setExecutor(this);
		Utils.getClassesOf(instance, instance.getPackageName() + ".command.commands", AbstractCommand.class).forEach(clazz -> {
			try {
				commands.add(clazz.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		for (AbstractCommand abstractCommand : commands) {
			// It's the main command
			if (arguments.length <= 0 && abstractCommand instanceof DeadByCraftCommand) {
				processRequirements(abstractCommand, sender, arguments);
				return true;
			} else if (arguments.length > 0 && abstractCommand.containsCommand(arguments[0])) {
				processRequirements(abstractCommand, sender, arguments);
				return true;
			}
		}
		new MessageBuilder("messages.command-doesnt-exist").send(sender);
		return true;
	}

	private void processRequirements(AbstractCommand command, CommandSender sender, String[] arguments) {
		if (!(sender instanceof Player) && !command.isConsoleAllowed()) {
			 new MessageBuilder("messages.must-be-player")
			 		.replace("%command%", command.getSyntax(sender))
			 		.setPlaceholderObject(sender)
			 		.send(sender);
			return;
		}
		if (command.getPermissionNodes() == null || Arrays.stream(command.getPermissionNodes()).parallel().anyMatch(permission -> sender.hasPermission(permission))) {
			if (command instanceof AdminCommand) {
				if (sender instanceof Player && !sender.hasPermission("deadbycraft.admin")) {
					new MessageBuilder("messages.no-permission").send(sender);
					return;
				}
			}
			String[] array = arguments;
			String entered = "deadbycraft";
			if (arguments.length > 0) {
				entered = array[0];
				array = Arrays.copyOfRange(arguments, 1, arguments.length);
			}
			ReturnType returnType = command.runCommand(entered, sender, array);
			if (returnType == ReturnType.SYNTAX_ERROR) {
				 new MessageBuilder("messages.invalid-command", "messages.invalid-command-correction")
				 		.replace("%command%", command.getSyntax(sender))
				 		.setPlaceholderObject(sender)
				 		.send(sender);
			}
			if (returnType != ReturnType.SUCCESS && sender instanceof Player)
				new SoundPlayer("error").playTo((Player) sender);
			return;
		}
		new MessageBuilder("messages.no-permission").send(sender);
	}

	public List<AbstractCommand> getCommands() {
		return Collections.unmodifiableList(commands);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
		for (AbstractCommand abstractCommand : commands) {
			if (arguments.length <= 0 && abstractCommand instanceof DeadByCraftCommand)
				return abstractCommand.onTabComplete(command, sender, alias, arguments);
			else if (arguments.length > 0 && abstractCommand.containsCommand(arguments[0]))
				return abstractCommand.onTabComplete(command, sender, alias, arguments);
		}
		return null;
	}

}
