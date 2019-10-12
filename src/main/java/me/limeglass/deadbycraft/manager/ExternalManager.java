package me.limeglass.deadbycraft.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import me.limeglass.deadbycraft.DeadByCraft;

public abstract class ExternalManager implements Listener {

	protected final String name;

	protected ExternalManager(String name, boolean listener) {
		this.name = name;
		if (listener)
			Bukkit.getPluginManager().registerEvents(this, DeadByCraft.getInstance());
	}

	public String getName() {
		return name;
	}

	public abstract boolean isEnabled();

}
