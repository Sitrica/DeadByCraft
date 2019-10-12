package me.limeglass.deadbycraft.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EpicSound {

	private final float pitch, volume;
	private final boolean enabled;
	private final Sound sound;
	private final int delay;

	public EpicSound(ConfigurationSection section, String fallback) {
		String name = section.getString("sound", "ENTITY_PLAYER_LEVELUP");
		this.sound = Utils.soundAttempt(name, fallback);
		this.volume = (float) section.getDouble("volume", 1);
		this.pitch = (float) section.getDouble("pitch", 1);
		this.enabled = section.getBoolean("enabled", true);
		this.delay = section.getInt("delay", 0);
	}

	public EpicSound(Sound sound, float pitch, float volume, boolean enabled) {
		this.enabled = enabled;
		this.volume = volume;
		this.sound = sound;
		this.pitch = pitch;
		this.delay = 0;
	}

	public int getDelay() {
		return delay;
	}

	public Sound getSound() {
		return sound;
	}

	public float getPitch() {
		return pitch;
	}

	public float getVolume() {
		return volume;
	}

	public void playTo(Player... players) {
		if (enabled) {
			for (Player player : players) {
				player.playSound(player.getLocation(), sound, volume, pitch);
			}
		}
	}

	public void playAt(Location... locations) {
		if (enabled) {
			for (Location location : locations) {
				location.getWorld().playSound(location, sound, volume, pitch);
			}
		}
	}

}
