package me.limeglass.deadbycraft.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.AbilityConfiguration;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.Role;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.IntervalUtils;
import me.limeglass.glowing.GlowingAPI;

public class Awareness extends Ability {

	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private final GlowingAPI glowing;

	public Awareness() {
		super(Role.SURVIVOR, Material.ENDER_EYE, "Awareness", "&7You gain the awareness to see teammates.", "&eEvery 2 minutes, you can", "&evisibly see all teammates.");
		DeadByCraft instance = DeadByCraft.getInstance();
		glowing = instance.getGlowingAPI();
		// Garbage collector.
		Bukkit.getScheduler().runTaskTimerAsynchronously(DeadByCraft.getInstance(), () -> {
			AbilityConfiguration abilityConfiguration = getAbilityConfiguration();
			FileConfiguration configuration = abilityConfiguration.getConfiguration();
			String interval = configuration.getString("cooldown-seconds", "2 minutes");
			long milliseconds = IntervalUtils.getMilliseconds(interval);
			Iterator<Entry<UUID, Long>> iterator = cooldowns.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<UUID, Long> entry = iterator.next();
				if (System.currentTimeMillis() - entry.getValue() > milliseconds)
					iterator.remove();
			}
		}, 0, 20 * 120); // 2 minutes
	}

	@Override
	public void initalizeConfiguration(FileConfiguration configuration) {
		if (!configuration.isSet("cooldown-seconds"))
			configuration.set("cooldown-seconds", "2 minutes");
		if (!configuration.isSet("lasting-seconds"))
			configuration.set("lasting-seconds", "10 seconds");
	}

	@Override
	public void tick(Game game, GamePlayer gamePlayer) {
		AbilityConfiguration abilityConfiguration = getAbilityConfiguration();
		FileConfiguration configuration = abilityConfiguration.getConfiguration();
		UUID uuid = gamePlayer.getUniqueId();
		Optional<Long> cooldown = Optional.ofNullable(cooldowns.get(uuid));
		if (cooldown.isPresent()) {
			String interval = configuration.getString("cooldown-seconds", "2 minutes");
			long milliseconds = IntervalUtils.getMilliseconds(interval);
			if (System.currentTimeMillis() - cooldown.get() > milliseconds)
				cooldowns.remove(uuid);
		} else {
			Optional<Player> player = gamePlayer.getPlayer();
			if (!player.isPresent())
				return;
			Set<Player> survivors = game.getBukkitSurvivors();
			String interval = configuration.getString("lasting-seconds", "10 seconds");
			long seconds = IntervalUtils.getInterval(interval) / 20;
			glowing.setTimedGlowing(seconds, TimeUnit.SECONDS, survivors, player.get());
			cooldowns.put(uuid, System.currentTimeMillis());
		}
	}

	@Override
	public void onGameStart(Game game, GamePlayer player) {}

	@Override
	public void onGameLeave(GamePlayer gamePlayer) {}

}
