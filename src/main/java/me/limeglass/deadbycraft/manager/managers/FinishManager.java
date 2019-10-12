package me.limeglass.deadbycraft.manager.managers;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.State;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.ListMessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class FinishManager extends Manager {

	public FinishManager() {
		super(false);
	}

	public enum FinishReason {
		MONSTER, ESCAPE, LEAVE;
	}

	public void finishGame(Game game, FinishReason reason) {
		game.setState(State.ENDING);
		DeadByCraft instance = DeadByCraft.getInstance();
		GameManager gameManager = instance.getManager(GameManager.class);
		switch (reason) {
			case ESCAPE:
				break;
			case LEAVE:
				for (GamePlayer player : game.getPlayers()) {
					Optional<Player> bukkit = player.getPlayer();
					if (!bukkit.isPresent())
						continue;
					long experience = game.getExperience(player);
					player.addExperience(experience);
					new SoundPlayer("finish.leave").playTo(bukkit.get());
					new ListMessageBuilder("finish.leave")
							.replace("%experience%", experience)
							.setPlaceholderObject(game)
							.send(bukkit.get());
				}
				break;
			case MONSTER:
				break;
			default:
				break;
		}
		Bukkit.getScheduler().runTaskLater(instance, () -> gameManager.stopGame(game), 20 * 3); // 5 seconds
	}

}
