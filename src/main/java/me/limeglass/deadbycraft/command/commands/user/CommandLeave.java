package me.limeglass.deadbycraft.command.commands.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AbstractCommand;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class CommandLeave extends AbstractCommand {

	public CommandLeave() {
		super(false, "leave", "l");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length != 0)
			return ReturnType.SYNTAX_ERROR;
		Player player = (Player) sender;
		DeadByCraft instance = DeadByCraft.getInstance();
		GamePlayer gamePlayer = instance.getManager(PlayerManager.class).getGamePlayer(player);
		if (!gamePlayer.getCurrentGame().isPresent()) {
			new MessageBuilder("commands.leave.not-in-an-arena")
					.setPlaceholderObject(gamePlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		GameManager gameManager = instance.getManager(GameManager.class);
		gameManager.leaveGame(gamePlayer);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "leave";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.leave", "deadbycraft.admin"};
	}

}
