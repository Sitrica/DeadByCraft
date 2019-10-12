package me.limeglass.deadbycraft.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.command.AdminCommand;
import me.limeglass.deadbycraft.manager.managers.SetupManager;
import me.limeglass.deadbycraft.manager.managers.SetupManager.Setup;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.ListMessageBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class SetupCommand extends AdminCommand {

	public SetupCommand() {
		super(false, "setup", "new", "create");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		SetupManager manager = DeadByCraft.getInstance().getManager(SetupManager.class);
		Optional<Setup> optional = manager.getSetup(player);
		if (!optional.isPresent()) {
			if (arguments.length != 0) {
				new MessageBuilder("setup.not-in-setup")
						.setPlaceholderObject(player)
						.send(player);
				return ReturnType.FAILURE;
			}
			manager.enterSetup(player);
			return ReturnType.SUCCESS;
		}
		Setup setup = optional.get();
		switch (arguments[0].toLowerCase()) {
			case "pos1":
				setup.setPos1(player.getTargetBlockExact(30).getLocation());
				new MessageBuilder("setup.pos1")
						.setPlaceholderObject(setup)
						.send(player);
				new ListMessageBuilder("setup.3")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "pos2":
				setup.setPos2(player.getTargetBlockExact(30).getLocation());
				new MessageBuilder("setup.pos2")
						.setPlaceholderObject(setup)
						.send(player);
				new ListMessageBuilder("setup.4")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "lobby":
				setup.setLobby(player.getLocation().add(0, 0.5, 0));
				new MessageBuilder("setup.lobby")
						.setPlaceholderObject(setup)
						.send(player);
				new ListMessageBuilder("setup.5")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "addspawn":
				int index = setup.addSpawn(player.getLocation().add(0, 1.3, 0));
				new MessageBuilder("setup.spawn")
						.setPlaceholderObject(setup)
						.replace("%spawn%", index + 1)
						.send(player);
				break;
			case "removespawn":
				if (arguments.length != 2)
					return ReturnType.SYNTAX_ERROR;
				int spot;
				try {
					spot = Integer.parseInt(arguments[1]);
				} catch (NumberFormatException e) {
					new MessageBuilder("setup.not-a-number")
							.setPlaceholderObject(setup)
							.replace("%input%", arguments[1])
							.send(player);
					return ReturnType.FAILURE;
				}
				spot = spot - 1;
				if (!setup.removeSpawn(spot)) {
					new MessageBuilder("setup.remove-fail")
							.setPlaceholderObject(setup)
							.replace("%input%", arguments[1])
							.send(player);
					return ReturnType.FAILURE;
				}
				new MessageBuilder("setup.removed-spawn")
						.setPlaceholderObject(setup)
						.replace("%spawn%", spot + 1)
						.send(player);
				break;
			case "addgenerator":
				//TODO get the block face and convert it to the direction, to allow the user to define which direction the schematic is placed.
				int indexGenerator = setup.addGenerator(player.getTargetBlockExact(30).getLocation().add(0, 1, 0));
				new MessageBuilder("setup.generator")
						.setPlaceholderObject(setup)
						.replace("%generator%", indexGenerator + 1)
						.send(player);
				new MessageBuilder("setup.6")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "removegenerator":
				if (arguments.length != 2)
					return ReturnType.SYNTAX_ERROR;
				int spotGenerator;
				try {
					spotGenerator = Integer.parseInt(arguments[1]);
				} catch (NumberFormatException e) {
					new MessageBuilder("setup.not-a-number")
							.setPlaceholderObject(setup)
							.replace("%input%", arguments[1])
							.send(player);
					return ReturnType.FAILURE;
				}
				spotGenerator = spotGenerator - 1;
				if (!setup.removeGenerator(spotGenerator)) {
					new MessageBuilder("setup.remove-fail")
							.setPlaceholderObject(setup)
							.replace("%input%", arguments[1])
							.send(player);
					return ReturnType.FAILURE;
				}
				new MessageBuilder("setup.removed-generator")
						.setPlaceholderObject(setup)
						.replace("%generator%", spotGenerator + 1)
						.send(player);
				break;
			case "gate":
				player.getInventory().addItem(new ItemStackBuilder("gate-tool").build());
				new ListMessageBuilder("setup.7")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "addgate":
				if (!manager.addGate(setup)) {
					new MessageBuilder("setup.gate-not-selected")
							.setPlaceholderObject(setup)
							.send(player);
					return ReturnType.FAILURE;
				}
				new MessageBuilder("setup.gate-added")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "addlever":
				int leverIndex = setup.addLever(player.getTargetBlockExact(30).getLocation().add(0, 1.3, 0));
				new MessageBuilder("setup.lever")
						.setPlaceholderObject(setup)
						.replace("%lever%", leverIndex + 1)
						.send(player);
				new ListMessageBuilder("setup.8")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "removelever":
				if (arguments.length != 2)
					return ReturnType.SYNTAX_ERROR;
				int spotLever;
				try {
					spotLever = Integer.parseInt(arguments[1]);
				} catch (NumberFormatException e) {
					new MessageBuilder("setup.not-a-number")
							.setPlaceholderObject(setup)
							.replace("%input%", arguments[1])
							.send(player);
					return ReturnType.FAILURE;
				}
				spotLever = spotLever - 1;
				if (!setup.removeLever(spotLever)) {
					new MessageBuilder("setup.remove-fail")
							.setPlaceholderObject(setup)
							.replace("%input%", arguments[1])
							.send(player);
					return ReturnType.FAILURE;
				}
				new MessageBuilder("setup.removed-lever")
						.setPlaceholderObject(setup)
						.replace("%lever%", spotLever + 1)
						.send(player);
				break;
			case "finish":
				if (!setup.isComplete()) {
					new MessageBuilder("setup.setup-not-completed")
							.setPlaceholderObject(setup)
							.send(player);
					return ReturnType.FAILURE;
				}
				manager.finish(setup);
				new ListMessageBuilder("setup.9")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "quit":
				new MessageBuilder("setup.quit")
						.setPlaceholderObject(setup)
						.send(player);
				manager.quit(player);
				break;
			default:
				return ReturnType.SYNTAX_ERROR;
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "setup";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"deadbycraft.setup", "deadbycraft.admin"};
	}

}
