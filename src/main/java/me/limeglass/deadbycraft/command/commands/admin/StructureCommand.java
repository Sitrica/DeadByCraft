package me.limeglass.deadbycraft.command.commands.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AdminCommand;
import me.limeglass.deadbycraft.manager.managers.StructureManager;
import me.limeglass.deadbycraft.manager.managers.StructureManager.StructureInfo;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class StructureCommand extends AdminCommand {

	public StructureCommand() {
		super(false, "structures", "structure");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length == 0)
			return ReturnType.SYNTAX_ERROR;
		Player player = (Player) sender;
		if (arguments[0].equalsIgnoreCase("tool")) {
			player.getInventory().addItem(new ItemStackBuilder("structure-tool").build());
			return ReturnType.SUCCESS;
		} else if (arguments.length != 2)
			return ReturnType.SYNTAX_ERROR;
		StructureManager structureManager = DeadByCraft.getInstance().getManager(StructureManager.class);
		switch (arguments[0].toLowerCase(Locale.US)) {
			case "p":
			case "paste":
				Optional<StructureInfo> info = structureManager.getStructure(arguments[1]);
				if (!info.isPresent()) {
					new MessageBuilder("commands.structures.no-structure")
							.replace("%structure%", arguments[1])
							.setPlaceholderObject(sender)
							.send(sender);
					return ReturnType.FAILURE;
				}
				structureManager.pasteStructure(info.get(), player.getLocation());
				break;
			case "s":
			case "save":
				try {
					boolean set = structureManager.saveStructure(arguments[1], player.getLocation(), player);
					if (set) {
						new MessageBuilder("commands.structures.saved")
								.replace("%structure%", arguments[1])
								.setPlaceholderObject(sender)
								.send(sender);
						return ReturnType.SUCCESS;
					} else {
						new MessageBuilder("commands.structures.not-set")
								.replace("%structure%", arguments[1])
								.setPlaceholderObject(sender)
								.send(sender);
						return ReturnType.FAILURE;
					}
				} catch (IOException e) {
					e.printStackTrace();
					new MessageBuilder("commands.structures.error")
							.replace("%structure%", arguments[1])
							.setPlaceholderObject(sender)
							.send(sender);
					return ReturnType.FAILURE;
				}
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "structures";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.structure", "deadbycraft.structures", "deadbycraft.admin"};
	}

	@Override
	protected List<String> onTabComplete(Command command, CommandSender sender, String alias, String... arguments) {
		if (arguments.length == 1)
			return super.onTabComplete(command, sender, alias, arguments);
		List<String> completions = new ArrayList<>();
		StringUtil.copyPartialMatches(arguments[1], Lists.newArrayList("paste", "save", "tool"), completions);
		Collections.sort(completions);
		return completions;
	}

}
