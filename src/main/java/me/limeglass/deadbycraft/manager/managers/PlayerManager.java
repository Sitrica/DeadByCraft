package me.limeglass.deadbycraft.manager.managers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.database.Database;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.IntervalUtils;

public class PlayerManager extends Manager {

	private final Set<GamePlayer> players = new HashSet<>();
	private Database<GamePlayer> database;

	public PlayerManager() {
		super(true);
		DeadByCraft instance = DeadByCraft.getInstance();
		FileConfiguration configuration = instance.getConfig();
		String table = configuration.getString("database.player-table", "Players");
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(table, GamePlayer.class);
		else
			database = getFileDatabase(table, GamePlayer.class);
		String interval = configuration.getString("database.autosave", "5 miniutes");
		Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> players.forEach(player -> database.put(player.getUniqueId() + "", player)), 0, IntervalUtils.getInterval(interval));
	}

	public GamePlayer getGamePlayer(Player player) {
		return getGamePlayer(player.getUniqueId()).orElseGet(() -> {
					GamePlayer p = new GamePlayer(player.getUniqueId());
					players.add(p);
					return p;
				});
	}

	public Optional<GamePlayer> getGamePlayer(OfflinePlayer player) {
		return getGamePlayer(player.getUniqueId());
	}

	public Optional<GamePlayer> getGamePlayer(UUID uuid) {
		return Optional.ofNullable(players.parallelStream()
				.filter(p -> p.getUniqueId().equals(uuid))
				.findFirst()
				.orElseGet(() -> {
					GamePlayer player = database.get(uuid + "");
					if (player != null)
						players.add(player);
					return player;
				}));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		GamePlayer player = getGamePlayer(event.getPlayer());
		DeadByCraft.debugMessage("Loaded player " + player.getUniqueId());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = getGamePlayer(player);
		database.put(player.getUniqueId() + "", gamePlayer);
		Bukkit.getScheduler().runTaskLaterAsynchronously(DeadByCraft.getInstance(), () -> players.removeIf(p -> p.getUniqueId().equals(player.getUniqueId())), 1);
	}

}
