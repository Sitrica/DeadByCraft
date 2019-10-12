package me.limeglass.deadbycraft.events;

import org.bukkit.event.HandlerList;

import me.limeglass.deadbycraft.objects.Game;

public class GameEndEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();

	public GameEndEvent(Game game) {
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
