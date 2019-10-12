package me.limeglass.deadbycraft.objects;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;

public class GamePlayer {

	private final Set<Loadout> loadouts = new HashSet<>();
	private ItemStack[] inventory;
	private Location location;
	private final UUID uuid;
	private long experience;
	private int level = 0;
	private Game game;

	public GamePlayer(UUID uuid) {
		this.uuid = uuid;
	}

	public void setCurrentGame(Game game) {
		this.game = game;
	}

	public void saveInventory() {
		getPlayer().ifPresent(player -> inventory = player.getInventory().getContents());
	}

	public void loadInventory() {
		getPlayer().ifPresent(player -> {
			PlayerInventory playerInventory = player.getInventory();
			playerInventory.clear();
			playerInventory.setContents(inventory);
		});
	}

	public Loadout getCharacterLoadout(GameCharacter character) {
		return loadouts.stream()
				.filter(loadout -> loadout.getCharacter().equals(character))
				.findFirst()
				.orElseGet(() -> {
					Loadout loadout = new Loadout(uuid, character);
					loadouts.add(loadout);
					return loadout;
				});
	}

	public int getLevel() {
		return level;
	}

	public long getExperience() {
		return experience;
	}

	public void setExperience(long experience) {
		this.experience = experience;
	}

	public void addExperience(long experience) {
		this.experience = this.experience + experience;
	}

	public void levelUp() {
		this.level = level + 1;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Set<Loadout> getLoadouts() {
		return loadouts;
	}

	public void addLoadout(Loadout loadout) {
		loadouts.add(loadout);
	}

	public Optional<Game> getCurrentGame() {
		return Optional.ofNullable(game);
	}

	public Location getJoinLocation() {
		return location;
	}

	public void setJoinLocation(Location location) {
		this.location = location;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Optional<Player> getPlayer() {
		return Optional.ofNullable(Bukkit.getPlayer(uuid));
	}

	public boolean isOnline() {
		return getPlayer().isPresent();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof GamePlayer))
			return false;
		GamePlayer other = (GamePlayer) object;
		if (!other.getUniqueId().equals(uuid))
			return false;
		return true;
	}

}
