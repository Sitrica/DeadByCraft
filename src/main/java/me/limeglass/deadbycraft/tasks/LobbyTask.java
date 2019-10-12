package me.limeglass.deadbycraft.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.utils.IntervalUtils;
import me.limeglass.deadbycraft.utils.MessageBuilder;

/**
 * A lobby task that checks when the lobby is ready to begin.
 * This is ran ASYNC so be careful. Ran every second.
 */
public class LobbyTask extends BukkitRunnable {

	private final DeadByCraft instance;
	private long seconds = 0, start;
	private final int minStart;
	private final Game game;

	public LobbyTask(DeadByCraft instance, Game game) {
		this.minStart = instance.getConfig().getInt("game.player-starts", 3);
		this.start = IntervalUtils.getInterval(instance.getConfig().getString("game.lobby-start-time", "30 seconds")) / 20;
		this.instance = instance;
		this.game = game;
	}

	@Override
	public void run() {
		GameManager gameManager = instance.getManager(GameManager.class);
		Set<Player> players = game.getBukkitPlayers();
		if (players.size() == 0) {
			Bukkit.getScheduler().runTask(instance, () -> gameManager.stopGame(game));
			cancel();
			return;
		}
		if (!game.isLobby()) {
			cancel();
			return;
		}
		if (players.size() >= minStart) {
			seconds = seconds + 1;
			if (seconds >= start) {
				if (game.getArenaInfo() == null) {
					List<ArenaInfo> infos = gameManager.getArenaInfos().stream()
							.collect(Collectors.toList());
					if (infos.size() == 0) {
						cancel();
						Bukkit.getScheduler().runTask(instance, () -> gameManager.stopGame(game));
						DeadByCraft.consoleMessage("There are no arenas setup! Cancelling lobby.");
						return;
					}
					Collections.shuffle(infos);
					game.setArenaInfo(infos.get(0));
				}
				Bukkit.getScheduler().runTask(instance, () -> gameManager.startGame(game));
				cancel();
				return;
			}
			if (!game.getMonster().isPresent())
				game.determineMonster();
			players.forEach(player -> new MessageBuilder(false, "lobby.starting-actionbar")
					.setPlaceholderObject(game)
					.replace("%time%", seconds)
					.sendActionbar(player));
		} else {
			seconds = 0;
			players.forEach(player -> new MessageBuilder(false, "lobby.waiting-actionbar")
					.setPlaceholderObject(game)
					.sendActionbar(player));
		}
	}

}
