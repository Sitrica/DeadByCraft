package me.limeglass.deadbycraft.command.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AbstractCommand;
import me.limeglass.deadbycraft.command.AdminCommand;
import me.limeglass.deadbycraft.utils.Formatting;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class DeadByCraftCommand extends AbstractCommand {
	
	public DeadByCraftCommand() {
		super(true, "dbc", "deadbycraft", "deadcraft");
	}

	@Override
	protected ReturnType runCommand(String input, CommandSender sender, String... args) {
		DeadByCraft instance = DeadByCraft.getInstance();
		sender.sendMessage("");
		new MessageBuilder("messages.version")
				.replace("%version%", instance.getDescription().getVersion())
				.send(sender);
		for (AbstractCommand command : instance.getCommandHandler().getCommands()) {
			if (command instanceof AdminCommand)
				continue;
			if (command.getConfigurationNode() == null)
				continue;
			if (command.getPermissionNodes() == null || Arrays.stream(command.getPermissionNodes()).parallel().anyMatch(permission -> sender.hasPermission(permission))) {
				sender.sendMessage(Formatting.color("&8 - &6" + command.getSyntax(sender) + "&7 - " + command.getDescription(sender)));
			}
		}
		sender.sendMessage("");
		return ReturnType.SUCCESS;
	}

	@Override
	public String[] getPermissionNodes() {
		return null;
	}

	@Override
	public String getConfigurationNode() {
		return "deadbycraft";
	}

}
