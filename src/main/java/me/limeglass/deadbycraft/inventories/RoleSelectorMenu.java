package me.limeglass.deadbycraft.inventories;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.Role;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class RoleSelectorMenu implements InventoryProvider {

	private final FileConfiguration inventories;
	private final PlayerManager playerManager;

	public RoleSelectorMenu() {
		DeadByCraft instance = DeadByCraft.getInstance();
		inventories = instance.getConfiguration("inventories").get();
		playerManager = instance.getManager(PlayerManager.class);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		if (inventories.getBoolean("inventories.role-selector.borders", true))
			contents.fillBorders(ClickableItem.empty(new ItemStackBuilder("inventories.role-selector.borders")
					.setPlaceholderObject(gamePlayer)
					.build()));
		setupContents(gamePlayer, contents);
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		// Make the update every 5 ticks
		int state = contents.property("state", 0);
		contents.setProperty("state", state + 1);
		if (state % 5 != 0)
			return;
		setupContents(playerManager.getGamePlayer(player), contents);
	}

	private void setupContents(GamePlayer gamePlayer, InventoryContents contents) {
		Optional<Game> game = gamePlayer.getCurrentGame();
		Player player = gamePlayer.getPlayer().get();
		contents.set(1, 3, ClickableItem.of(new ItemStackBuilder("inventories.role-selector.survivor")
				.glowingIf(() -> game.isPresent() ? game.get().hasRequested(gamePlayer, Role.SURVIVOR) : false)
				.setPlaceholderObject(gamePlayer)
				.build(), event -> {
					if (!game.isPresent())
						return;
					game.get().requestRole(gamePlayer, Role.SURVIVOR);
					new MessageBuilder("lobby.request-survivor")
							.setPlaceholderObject(game.get())
							.send(player);
					new SoundPlayer("lobby.select-survivor").playTo(player);
				}));
		contents.set(1, 5, ClickableItem.of(new ItemStackBuilder("inventories.role-selector.monster")
				.glowingIf(() -> game.isPresent() ? game.get().hasRequested(gamePlayer, Role.MONSTER) : false)
				.setPlaceholderObject(gamePlayer)
				.build(), event -> {
					if (!game.isPresent())
						return;
					game.get().requestRole(gamePlayer, Role.MONSTER);
					new MessageBuilder("lobby.request-monster")
							.setPlaceholderObject(game.get())
							.send(player);
					new SoundPlayer("lobby.select-monster").playTo(player);
				}));
	}

}
