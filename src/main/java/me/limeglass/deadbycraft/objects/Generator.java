package me.limeglass.deadbycraft.objects;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.GameManager;

public class Generator {

	private final Location origin;
	private final long complete;
	private final World world;
	private int progress = 0;
	private boolean running;

	public Generator(Location origin, long complete) {
		this.world = origin.getWorld();
		this.complete = complete;
		this.origin = origin;
	}

	public Set<Player> getNearbyPlayers(double radius) {
		GameManager gameManager = DeadByCraft.getInstance().getManager(GameManager.class);
		return world.getNearbyEntities(origin, radius, radius, radius,
				entity -> entity.getType() == EntityType.PLAYER).stream()
				.map(entity -> (Player) entity)
				.filter(player -> !gameManager.isMonster(player))
				.collect(Collectors.toSet());
	}

	public void increaseProgress() {
		progress = progress + 1;
		if (progress > complete)
			running = true;
	}

	public void decreaseProgress() {
		progress = progress - 1;
		if (progress < 0)
			progress = 0;
	}

	public long getCompleteTime() {
		return complete;
	}

	public int getProgress() {
		return progress;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isRunning() {
		return running;
	}

	public World getWorld() {
		return world;
	}

}
