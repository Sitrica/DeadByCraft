package me.limeglass.deadbycraft.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AbstractCommand;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class CommandJoin extends AbstractCommand {

	public CommandJoin() {
		super(false, "join", "j");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length <= 0)
			return ReturnType.SYNTAX_ERROR;
		Player player = (Player) sender;
		DeadByCraft instance = DeadByCraft.getInstance();
		GamePlayer gamePlayer = instance.getManager(PlayerManager.class).getGamePlayer(player);
		if (gamePlayer.getCurrentGame().isPresent()) {
			new MessageBuilder("commands.join.already-in-a-game")
					.setPlaceholderObject(gamePlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		GameManager gameManager = instance.getManager(GameManager.class);
		Optional<ArenaInfo> info = gameManager.getArenaInfo(arguments[0]);
		if (!info.isPresent()) {
			new MessageBuilder("commands.join.not-an-arena")
					.setPlaceholderObject(gamePlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		Game game = gameManager.getOrCreateGame(info.get());
		gameManager.joinGame(gamePlayer, game);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "join";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.join", "deadbycraft.admin"};
	}

}
