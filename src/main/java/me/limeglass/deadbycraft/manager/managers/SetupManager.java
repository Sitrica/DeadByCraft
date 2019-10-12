package me.limeglass.deadbycraft.manager.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Sets;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.inventories.AnvilMenu;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.BlockInfo;
import me.limeglass.deadbycraft.utils.CuboidRegion;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.ListMessageBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class SetupManager extends Manager {

	private final Map<Player, Location> position1 = new HashMap<>(), position2 = new HashMap<>();
	private final Set<Setup> setups = new HashSet<>();

	public SetupManager() {
		super(true);
	}

	public Optional<Setup> getSetup(Player player) {
		return setups.parallelStream()
				.filter(setup -> setup.player.equals(player))
				.findFirst();
	}

	public void finish(Setup setup) {
		CuboidRegion region = new CuboidRegion(setup.getLocation("pos1").get(), setup.getLocation("pos2").get());
		ArenaInfo info = new ArenaInfo(setup.getName(), region, setup.getLocation("lobby").get());
		setup.spawns.forEach(spawn -> info.addSpawn(spawn));
		setup.levers.forEach(lever -> info.addLever(lever));
		setup.generators.forEach(generator -> info.addGenerator(generator));
		info.saveBlocks(setup);
		info.setGates(setup.getGates());
		DeadByCraft.getInstance().getManager(GameManager.class).addArenaInfo(info);
		setups.remove(setup);
	}

	public boolean addGate(Setup setup) {
		Player player = setup.getPlayer();
		if (!position1.containsKey(player) && !position2.containsKey(player))
			return false;
		CuboidRegion region = new CuboidRegion(position1.get(player), position2.get(player));
		Iterator<Block> iterator = region.iterator();
		while (iterator.hasNext()) {
			BlockInfo info = new BlockInfo(iterator.next());
			setup.addGatePart(info);
		}
		return true;
	}

	public Setup enterSetup(Player player) {
		new ListMessageBuilder("setup.1")
				.setPlaceholderObject(player)
				.send(player);
		new SoundPlayer("setup.enter").playTo(player);
		Setup setup = new Setup(player);
		FileConfiguration inventories = DeadByCraft.getInstance().getConfiguration("inventories").get();
		ItemStack search = new ItemStackBuilder(inventories.getConfigurationSection("inventories.setup.name-anvil"))
				.setPlaceholderObject(player)
				.build();
		new AnvilMenu(search, player, name -> {
			if (name.contains(" ")) {
				new MessageBuilder("setup.no-spaces")
						.setPlaceholderObject(player)
						.replace("%name%", name)
						.send(player);
				new SoundPlayer("error").playTo(player);
				return;
			}
			if (DeadByCraft.getInstance().getManager(GameManager.class).getArenaInfo(name).isPresent()) {
				new MessageBuilder("setup.already-exists")
						.setPlaceholderObject(player)
						.replace("%name%", name)
						.send(player);
				new SoundPlayer("error").playTo(player);
				return;
			}
			setups.add(setup);
			setup.setName(name);
			new ListMessageBuilder("setup.2")
					.setPlaceholderObject(player)
					.replace("%name%", name)
					.send(player);
		});
		return setup;
	}

	public void quit(Player player) {
		getSetup(player).ifPresent(setup -> setups.remove(setup));
		player.closeInventory();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		quit(event.getPlayer());
	}

	public class Setup {

		private final Map<String, Location> singles = new HashMap<>();
		private final List<Location> generators = new ArrayList<>();
		private final List<Location> levers = new ArrayList<>();
		private final List<Location> spawns = new ArrayList<>();
		private final List<BlockInfo> gates = new ArrayList<>();
		private final Player player;
		private String name;

		public Setup(Player player) {
			this.player = player;
		}

		public String getName() {
			return name;
		}

		public List<BlockInfo> getGates() {
			return gates;
		}

		public Player getPlayer() {
			return player;
		}

		public void addGatePart(BlockInfo info) {
			gates.add(info);
		}

		public void addGate(Collection<BlockInfo> infos) {
			gates.addAll(infos);
		}

		public void setName(String name) {
			this.name = name;
		}

		public int addSpawn(Location spawn) {
			spawns.add(spawn);
			return spawns.size() - 1;
		}

		public boolean removeSpawn(int index) {
			if (index >= spawns.size() || index < 0)
				return false;
			spawns.remove(index);
			return true;
		}

		public int addLever(Location lever) {
			levers.add(lever);
			return levers.size() - 1;
		}

		public boolean removeLever(int index) {
			if (index >= levers.size() || index < 0)
				return false;
			levers.remove(index);
			return true;
		}

		public int addGenerator(Location generator) {
			generators.add(generator);
			return generators.size() - 1;
		}

		public boolean removeGenerator(int index) {
			if (index >= generators.size() || index < 0)
				return false;
			generators.remove(index);
			return true;
		}

		public Optional<Location> getLocation(String key) {
			return Optional.ofNullable(singles.get(key));
		}

		public void setPos1(Location pos1) {
			singles.put("pos1", pos1);
		}

		public void setPos2(Location pos2) {
			singles.put("pos2", pos2);
		}

		public void setLobby(Location lobby) {
			singles.put("lobby", lobby);
		}

		public boolean isComplete() {
			return !singles.isEmpty() && !spawns.isEmpty() && name != null && !generators.isEmpty();
		}

	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (event.getHand() != EquipmentSlot.HAND)
			return;
		Player player = event.getPlayer();
		if (!Sets.newHashSet("deadbycraft.setup", "deadbycraft.admin").stream()
				.anyMatch(permission -> player.hasPermission(permission)))
			return;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null)
			return;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return;
		ItemStack tool = new ItemStackBuilder("gate-tool").build();
		ItemMeta toolMeta = tool.getItemMeta();
		if (toolMeta == null)
			return;
		if (tool.getType() != item.getType())
			return;
		if (!meta.getDisplayName().equalsIgnoreCase(toolMeta.getDisplayName()))
			return;
		event.setCancelled(true);
		Location location = event.getClickedBlock().getLocation();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			position2.put(player, location);
			new MessageBuilder("setup.gate-selected-pos2")
					.replace("%location%", location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ())
					.setPlaceholderObject(player)
					.send(player);
			if (position1.containsKey(player) && position2.containsKey(player))
				new MessageBuilder("setup.gate-ready")
						.setPlaceholderObject(player)
						.send(player);
			return;
		}
		position1.put(player, location);
		new MessageBuilder("setup.gate-selected-pos1")
				.replace("%location%", location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ())
				.setPlaceholderObject(player)
				.send(player);
		if (position1.containsKey(player) && position2.containsKey(player))
			new MessageBuilder("setup.gate-ready")
					.setPlaceholderObject(player)
					.send(player);
	}

}
