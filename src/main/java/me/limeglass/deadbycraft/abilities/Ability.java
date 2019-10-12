package me.limeglass.deadbycraft.abilities;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.CharacterManager;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.AbilityConfiguration;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.Role;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;

public abstract class Ability {

	private final String[] description;
	private final Material icon;
	private final String name;
	private final Role role;

	public Ability(Role role, Material icon, String name, String... description) {
		this.description = description;
		this.name = name;
		this.role = role;
		this.icon = icon;
	}

	public AbilityConfiguration getAbilityConfiguration() {
		return DeadByCraft.getInstance().getManager(CharacterManager.class).getAbilityConfiguration(this);
	}

	public abstract void initalizeConfiguration(FileConfiguration configuration);

	public ItemStackBuilder getIcon() {
		return getAbilityConfiguration().getIcon();
	}

	public String[] getDescription() {
		return description;
	}

	public Material getMaterial() {
		return icon;
	}

	public String getName() {
		return name;
	}

	public Role getRole() {
		return role;
	}

	/**
	 * Every second this method will be called, used on abilities that depends on time.
	 * 
	 * @param game The Game the ability is being used in.
	 * @param player The GamePlayer using the ability.
	 */
	public abstract void tick(Game game, GamePlayer player);

	/**
	 * Called when the game starts.
	 * 
	 * @param game The Game the ability is being used in.
	 * @param player The GamePlayer using the ability.
	 */
	public abstract void onGameStart(Game game, GamePlayer player);

	/**
	 * Called when a player leaves the game.
	 * 
	 * @param player GamePlayer that is leaving the game.
	 */
	public abstract void onGameLeave(GamePlayer player);

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Ability))
			return false;
		Ability other = (Ability) object;
		return other.name.equalsIgnoreCase(name);
	}

}
