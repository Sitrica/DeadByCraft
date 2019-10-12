package me.limeglass.deadbycraft.objects;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockInfo {

	private final String material, data;
	private final Location location;

	public BlockInfo(Block block) {
		this.data = block.getBlockData().getAsString();
		this.material = block.getType().name();
		this.location = block.getLocation();
	}

	public BlockInfo(Location location, String material, String data) {
		this.location = location;
		this.material = material;
		this.data = data;
	}

	public Material getMaterial() {
		return Optional.ofNullable(Material.getMaterial(material))
				.orElse(Material.getMaterial(material, true));
	}

	public BlockData getBlockData() {
		return Bukkit.createBlockData(data);
	}

	public Location getLocation() {
		 return location;
	}

	public int getX() {
		return location.getBlockX();
	}

	public int getY() {
		return location.getBlockY();
	}

	public int getZ() {
		return location.getBlockZ();
	}

}
