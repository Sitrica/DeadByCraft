package me.limeglass.deadbycraft.manager.managers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.utils.Formatting;
import me.limeglass.deadbycraft.utils.Utils;

public class ActionbarManager extends Manager {

	// Caching
	private final boolean classes, method;

	public ActionbarManager() {
		super(false);
		this.classes = Utils.classExists("net.md_5.bungee.api.ChatMessageType") && Utils.classExists("net.md_5.bungee.api.chat.TextComponent");
		if (!classes) {
			method = false;
			return;
		}
		this.method = Utils.methodExists(Player.Spigot.class, "sendMessage", ChatMessageType.class, BaseComponent.class);
	}

	public void sendActionBar(Collection<Player> players, String... messages) {
		players.forEach(player -> sendActionBar(player, messages));
	}

	public void sendActionBar(Player player, String... messages) {
		if (classes && method) {
			for (String message : messages) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Formatting.color(message)));
			}
		}
	}

}
