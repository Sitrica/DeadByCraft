package me.limeglass.deadbycraft.objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockLocation {

	private final World world;
	private final int x, y, z;

	public BlockLocation(World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Location getLocation() {
		return new Location(world, x, y, z);
	}

	public Block getBlock() {
		return world.getBlockAt(x, y, z);
	}

}
