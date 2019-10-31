package me.limeglass.deadbycraft.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.ActionbarManager;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.manager.managers.SetupManager.Setup;
import me.limeglass.deadbycraft.utils.CuboidRegion;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class ArenaInfo {

	private final List<Location> generators = new ArrayList<>();
	private final List<Location> levers = new ArrayList<>();
	private final List<Location> spawns = new ArrayList<>();
	private final List<BlockInfo> gates = new ArrayList<>();
	private final Set<BlockInfo> blocks = new HashSet<>();
	private final CuboidRegion region;
	private final Location lobby;
	private int max = 5, min = 2;
	private final String name;
	private long saving;

	public ArenaInfo(String name, CuboidRegion region, Location lobby) {
		this.region = region;
		this.lobby = lobby;
		this.name = name;
	}

	public void addSpawn(Location spawn) {
		spawns.add(spawn);
	}

	public List<Location> getSpawns() {
		return spawns;
	}

	public void addLever(Location lever) {
		levers.add(lever);
	}

	public List<Location> getLevers() {
		return levers;
	}

	public void setGates(Collection<BlockInfo> infos) {
		gates.clear();
		gates.addAll(infos);
	}

	public List<BlockInfo> getGates() {
		return gates;
	}

	public void removeSpawn(int index) {
		if (index >= spawns.size() || index < 0)
			return;
		spawns.remove(index);
	}

	public void addGenerator(Location generator) {
		generators.add(generator);
	}

	public List<Location> getGenerators() {
		return generators;
	}

	public void removeGenerator(int index) {
		if (index >= generators.size() || index < 0)
			return;
		generators.remove(index);
	}

	public boolean isSaving() {
		return saving > 0;
	}

	public String getSavingProgress() {
		return saving + "/" + region.getArea();
	}

	public void saveBlocks(Setup setup) {
		DeadByCraft instance = DeadByCraft.getInstance();
		Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
			int i = 0;
			for (BlockLocation location : region.getBlockLocations()) {
				i++;
				saving++;
				if (i >= 3500) {
					try {
						instance.getManager(ActionbarManager.class).sendActionBar(setup.getPlayer(), "Progress: " + ChatColor.YELLOW + getSavingProgress());
						Thread.sleep(50);
					} catch (InterruptedException e) {}
					i = 0;
				}
				Bukkit.getScheduler().runTask(instance, () -> blocks.add(new BlockInfo(location.getBlock())));
			}
			instance.getManager(ActionbarManager.class).sendActionBar(setup.getPlayer(), ChatColor.GREEN + "Total of " + blocks.size() + " blocks saved.");
			new MessageBuilder("setup.complete")
					.setPlaceholderObject(setup)
					.send(setup.getPlayer());
			instance.getManager(GameManager.class).save(this);
		});
	}

	public Location getLobby() {
		return lobby;
	}

	public int getMinimumPlayers() {
		return min;
	}

	public int getMaximumPlayers() {
		return max;
	}

	public void setMaximumPlayers(int max) {
		this.max = max;
	}

	public void setMinimumPlayers(int min) {
		this.min = min;
	}

	public void rebuild() {
		region.forEach(block -> {
			Optional<BlockInfo> optional = getBlockInfo(block.getLocation());
			if (!optional.isPresent())
				return;
			BlockInfo info = optional.get();
			if (block.getType() != info.getMaterial())
				block.setType(info.getMaterial());
			if (!block.getBlockData().getAsString().equals(info.getBlockData().getAsString()))
				block.setBlockData(info.getBlockData());
		});
		gates.forEach(info -> {
			Block block = info.getLocation().getBlock();
			if (block.getType() != info.getMaterial())
				block.setType(info.getMaterial());
			if (!block.getBlockData().getAsString().equals(info.getBlockData().getAsString()))
				block.setBlockData(info.getBlockData());
		});
	}

	public CuboidRegion getRegion() {
		return region;
	}

	public String getName() {
		return name;
	}

	public Optional<BlockInfo> getBlockInfo(Location location) {
		return blocks.stream()
				.filter(info -> info.getLocation().equals(location))
				.findFirst();
	}

	public Set<BlockInfo> getSavedBlocks() {
		return blocks;
	}

}
