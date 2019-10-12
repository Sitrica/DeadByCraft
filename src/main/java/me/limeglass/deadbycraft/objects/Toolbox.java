package me.limeglass.deadbycraft.objects;

import org.bukkit.block.Block;

public class Toolbox {

	private final long creation = System.currentTimeMillis();
	private final GamePlayer placer;
	private final long expiration;
	private final Block block;

	public Toolbox(Block block, GamePlayer placer, long expiration) {
		this.expiration = expiration;
		this.placer = placer;
		this.block = block;
	}

	public GamePlayer getPlacer() {
		return placer;
	}

	public long getExpiration() {
		return expiration;
	}

	public long getCreation() {
		return creation;
	}

	public Block getBlock() {
		return block;
	}

}
