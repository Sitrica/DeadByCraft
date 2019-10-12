package me.limeglass.deadbycraft.events;

import org.bukkit.event.Event;

import me.limeglass.deadbycraft.objects.Game;

public abstract class GameEvent extends Event {

	private final Game game;

	public GameEvent(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

}
