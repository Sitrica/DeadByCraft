package me.limeglass.deadbycraft.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import me.limeglass.deadbycraft.abilities.Ability;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;

public class Loadout {

	private final Map<Integer, Ability> slots = new HashMap<>();
	private final GameCharacter character;
	private final UUID player;

	public Loadout(UUID player, GameCharacter character) {
		this.character = character;
		this.player = player;
	}

	public Optional<Ability> getAbilitySlot(int slot) {
		return Optional.ofNullable(slots.get(slot));
	}

	public void setAbilitySlot(int slot, Ability ability) {
		slots.put(slot, ability);
	}

	public void removeAbilitySlot(int slot) {
		slots.remove(slot);
	}

	public void setNextSlot(Ability ability) {
		for (int slot = 0; slot < 5; slot++) {
			if (!getAbilitySlot(slot).isPresent()) {
				slots.put(slot, ability);
				return;
			}
		}
	}

	public Map<Integer, Ability> getAbilities() {
		return slots;
	}

	public GameCharacter getCharacter() {
		return character;
	}

	public UUID getPlayerUUID() {
		return player;
	}

}
