package me.limeglass.deadbycraft.manager.managers.external;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.ExternalManager;

public class CitizensManager extends ExternalManager {

	private Plugin citizens;

	public CitizensManager() {
		super("citizens", false);
		citizens = CitizensAPI.getPlugin();
		if (citizens != null)
			DeadByCraft.consoleMessage("Hooked into Citizens!");
	}

	public boolean isCitizen(Entity entity) {
		if (entity == null)
			return false;
		if (citizens == null)
			return false;
		if (entity.hasMetadata("NPC"))
			return true;
		if (CitizensAPI.getNPCRegistry().isNPC(entity))
			return true;
		return false;
	}

	@Override
	public boolean isEnabled() {
		return citizens != null;
	}

}
