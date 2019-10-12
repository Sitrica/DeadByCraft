package me.limeglass.deadbycraft.command.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AdminCommand;
import me.limeglass.deadbycraft.manager.managers.DataManager;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class SpawnCommand extends AdminCommand {

	public SpawnCommand() {
		super(false, "setspawn", "spawn");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length > 0)
			return ReturnType.SYNTAX_ERROR;
		Player player = (Player) sender;
		DataManager manager = DeadByCraft.getInstance().getManager(DataManager.class);
		if (command.equalsIgnoreCase("spawn")) {
			manager.getGlobalSpawn().ifPresent(location -> player.teleport(location));
			return ReturnType.SUCCESS;
		}
		manager.setGlobalSpawn(player.getLocation().add(0, 0.5, 0));
		new MessageBuilder("commands.setspawn.set")
				.setPlaceholderObject(sender)
				.send(sender);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "setspawn";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.setspawn", "deadbycraft.admin"};
	}

}
