package me.limeglass.deadbycraft.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.GamePlayer;

public class PlayerJoinGameEvent extends GameEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final GamePlayer player;
	private boolean cancelled;

	public PlayerJoinGameEvent(Game game, GamePlayer player) {
		super(game);
		this.player = player;
	}

	public GamePlayer getGamePlayer() {
		return player;
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
