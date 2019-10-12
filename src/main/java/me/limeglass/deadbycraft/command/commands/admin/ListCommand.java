package me.limeglass.deadbycraft.command.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AdminCommand;
import me.limeglass.deadbycraft.manager.managers.GameManager;

public class ListCommand extends AdminCommand {

	public ListCommand() {
		super(true, "list", "l");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length != 0)
			return ReturnType.SYNTAX_ERROR;
		GameManager manager = DeadByCraft.getInstance().getManager(GameManager.class);
		manager.getArenaInfos().forEach(info -> {
			sender.sendMessage(ChatColor.GRAY + info.getName() + (manager.isGameRunning(info.getName()) ? " - " + ChatColor.GREEN + "Running" : info.isSaving() ? " - " + ChatColor.YELLOW + "Saving " + info.getSavingProgress() : ""));
		});
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "list";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.list", "deadbycraft.admin"};
	}

}
