package me.limeglass.deadbycraft.manager.managers;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.Manager;

public class DataManager extends Manager {

	private final FileConfiguration data;

	public DataManager() {
		super(false);
		this.data = DeadByCraft.getInstance().getConfiguration("data").get();
	}

	public Optional<Location> getGlobalSpawn() {
		return Optional.ofNullable(data.getSerializable("data.spawn", Location.class));
	}

	public void setGlobalSpawn(Location spawn) {
		data.set("data.spawn", spawn);
	}

}
