package me.limeglass.deadbycraft.manager.managers;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.minuskube.inv.SmartInventory;
import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.inventories.MonsterMenu;
import me.limeglass.deadbycraft.inventories.RoleSelectorMenu;
import me.limeglass.deadbycraft.inventories.SurvivorMenu;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.Role;
import me.limeglass.deadbycraft.objects.Game.RoleRequest;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class GameMechanicManager extends Manager {

	private final FileConfiguration inventories;
	private final boolean bungeecord;

	public GameMechanicManager() {
		super(true);
		DeadByCraft instance = DeadByCraft.getInstance();
		bungeecord = instance.getConfig().getBoolean("general.bungeecord", false);
		inventories = instance.getConfiguration("inventories").get();
	}

	public void giveLobbyItems(GamePlayer gamePlayer) {
		if (!gamePlayer.isOnline())
			return;
		Player player = gamePlayer.getPlayer().get();
		player.getInventory().setItem(3, new ItemStackBuilder("lobby-items.role-selector")
				.setPlaceholderObject(gamePlayer)
				.build());
		player.getInventory().setItem(5, new ItemStackBuilder("lobby-items.character-selector")
				.setPlaceholderObject(gamePlayer)
				.build());
		if (bungeecord)
			player.getInventory().setItem(4, new ItemStackBuilder("lobby-items.map-selector")
					.setPlaceholderObject(gamePlayer)
					.build());
	}

	public int getLobbyItem(GamePlayer gamePlayer, ItemStack itemstack) {
		ItemStack built = new ItemStackBuilder("lobby-items.role-selector")
				.setPlaceholderObject(gamePlayer)
				.build();
		if (itemstack.isSimilar(built))
			return 1;
		built = new ItemStackBuilder("lobby-items.character-selector")
				.setPlaceholderObject(gamePlayer)
				.build();
		if (itemstack.isSimilar(built))
			return 2;
		built = new ItemStackBuilder("lobby-items.map-selector")
				.setPlaceholderObject(gamePlayer)
				.build();
		if (itemstack.isSimilar(built))
			return 3;
		return -1;
	}

	@EventHandler
	public void onLobbyItems(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL)
			return;
		ItemStack item = event.getItem();
		if (item == null)
			return;
		Player player = event.getPlayer();
		GamePlayer gamePlayer = DeadByCraft.getInstance().getManager(PlayerManager.class).getGamePlayer(player);
		switch (getLobbyItem(gamePlayer, item)) {
			case 1:
				SmartInventory.builder()
						.title(new MessageBuilder(false, "inventories.role-selector.title")
								.setPlaceholderObject(gamePlayer)
								.fromConfiguration(inventories)
								.get())
						.manager(DeadByCraft.getInventoryManager())
						.provider(new RoleSelectorMenu())
						.id("role-selector")
						.size(3, 9)
						.build()
						.open(player);
				break;
			case 2:
				openCurrentMenu(gamePlayer);
				break;
			case 3:
				break;
		}
	}

	public void openCurrentMenu(GamePlayer gamePlayer) {
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		Optional<Player> optionalPlayer = gamePlayer.getPlayer();
		if (!optionalPlayer.isPresent())
			return;
		Player player = optionalPlayer.get();
		new SoundPlayer("click").playTo(player);
		Optional<RoleRequest> request = game.getRoleRequest(gamePlayer);
		if (request.isPresent()) {
			if (request.get().getRequestedRole() == Role.MONSTER)
				new MonsterMenu().open(player);
			else
				new SurvivorMenu().open(player);
			return;
		}
		new SurvivorMenu().open(player);
	}

}
