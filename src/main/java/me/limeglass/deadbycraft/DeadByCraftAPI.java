package me.limeglass.deadbycraft;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.Validate;

import me.limeglass.deadbycraft.manager.managers.GameManager;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.Game;

public class DeadByCraftAPI {

	private static DeadByCraft instance;
	private static GameManager manager;

	public static void setInstance(DeadByCraft instance) {
		Validate.isTrue(DeadByCraftAPI.instance == null, "The API instance can not be set twice!");
		DeadByCraftAPI.instance = instance;
		manager = instance.getManager(GameManager.class);
	}

	/**
	 * Get all the running games.
	 * 
	 * @return Set<Game> of all running gmaes.
	 */
	public static Set<Game> getRunningGames() {
		return manager.getRunningGames();
	}

	/**
	 * Grab a game by name if it's running.
	 * 
	 * @param name The name of the Arena to search for.
	 * @return Optional<Game> the game with the matching name if found running.
	 */
	public static Optional<Game> getRunningGame(String name) {
		return manager.getRunningGame(name);
	}

	/**
	 * Grab arena information by name, Arena information contains all the data about the arena, not the running game.
	 * 
	 * @param name The name of the ArenaInfo to search for.
	 * @return Optional<ArenaInfo> the ArenaInfo with the matching name if found.
	 */
	public static Optional<ArenaInfo> getArenaInfo(String name) {
		return manager.getArenaInfo(name);
	}

}
