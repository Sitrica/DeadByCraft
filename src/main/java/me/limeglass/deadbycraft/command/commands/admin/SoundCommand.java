package me.limeglass.deadbycraft.command.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AdminCommand;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class SoundCommand extends AdminCommand {

	public SoundCommand() {
		super(false, "sound", "soundtest");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length != 1)
			return ReturnType.SYNTAX_ERROR;
		Player player = (Player) sender;
		FileConfiguration sounds = DeadByCraft.getInstance().getConfiguration("sounds").get();
		if (!sounds.isConfigurationSection(arguments[0])) {
			new MessageBuilder("commands.sound.no-configuration-section")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		new SoundPlayer(sounds.getConfigurationSection(arguments[0])).playTo(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "sound";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.sound", "deadbycraft.admin"};
	}

}
