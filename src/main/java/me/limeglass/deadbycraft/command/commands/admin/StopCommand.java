package me.limeglass.deadbycraft.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AdminCommand;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class StopCommand extends AdminCommand {

	public StopCommand() {
		super(true, "stop", "end");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length <= 0)
			return ReturnType.SYNTAX_ERROR;
		GameManager manager = DeadByCraft.getInstance().getManager(GameManager.class);
		Optional<ArenaInfo> optional = manager.getArenaInfo(arguments[0]);
		if (!optional.isPresent()) {
			new MessageBuilder("arenas.no-arena-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		ArenaInfo info = optional.get();
		Optional<Game> game = manager.getRunningGame(info.getName());
		if (!game.isPresent()) {
			new MessageBuilder("arenas.no-running-game-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		new MessageBuilder("commands.stop.stopped")
				.setPlaceholderObject(game.get())
				.send(game.get().getBukkitPlayers());
		manager.stopGame(game.get());
		new MessageBuilder("commands.stop.stopped")
				.replace("%input%", arguments[0])
				.setPlaceholderObject(sender)
				.send(sender);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "stop";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.stop", "deadbycraft.admin"};
	}

}
