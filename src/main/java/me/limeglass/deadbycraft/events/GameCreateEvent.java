package me.limeglass.deadbycraft.events;

import org.bukkit.event.HandlerList;

import me.limeglass.deadbycraft.objects.Game;

/**
 * When a Game is created and thrown into Lobby mode waiting for players.
 */
public class GameCreateEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();

	public GameCreateEvent(Game game) {
		super(game);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
