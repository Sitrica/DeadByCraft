package me.limeglass.deadbycraft.events;

import org.bukkit.event.HandlerList;

import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.GamePlayer;

public class PlayerLeaveGameEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();
	private final GamePlayer player;

	public PlayerLeaveGameEvent(Game game, GamePlayer player) {
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

}
