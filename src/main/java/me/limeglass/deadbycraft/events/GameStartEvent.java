package me.limeglass.deadbycraft.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import me.limeglass.deadbycraft.objects.Game;

public class GameStartEvent extends GameEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;

	public GameStartEvent(Game game) {
		super(game);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
