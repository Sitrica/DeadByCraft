package me.limeglass.deadbycraft.inventories;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.CharacterManager;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.Role;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.ListMessageBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class MonsterMenu implements InventoryProvider {

	private final CharacterManager characterManager;
	private final FileConfiguration inventories;
	private final FileConfiguration characters;
	private final PlayerManager playerManager;
	private final SmartInventory inventory;
	private final boolean duplicates;

	public MonsterMenu() {
		DeadByCraft instance = DeadByCraft.getInstance();
		duplicates = instance.getConfig().getBoolean("game.allow-only-one-character", false);
		characterManager = instance.getManager(CharacterManager.class);
		inventories = instance.getConfiguration("inventories").get();
		characters = instance.getConfiguration("characters").get();
		playerManager = instance.getManager(PlayerManager.class);
		inventory =  SmartInventory.builder()
				.title(new MessageBuilder(false, "inventories.character-selector.title")
						.fromConfiguration(inventories)
						.get())
				.manager(DeadByCraft.getInventoryManager())
				.id("monster-selector")
				.provider(this)
				.size(6, 9)
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		ClickableItem border = ClickableItem.empty(new ItemStackBuilder("inventories.character-selector.monster-border")
				.setPlaceholderObject(gamePlayer)
				.build());
		contents.fillColumn(2, border);
		contents.fillColumn(8, border);
		contents.fillRow(0, border);
		contents.fillRow(5, border);
		ClickableItem basicBorder = ClickableItem.empty(new ItemStackBuilder("inventories.character-selector.basic-border")
				.setPlaceholderObject(gamePlayer)
				.build());
		contents.fillColumn(0, basicBorder);
		contents.fillColumn(1, basicBorder);
		contents.set(1, 1, border);
		contents.set(0, 0, ClickableItem.empty(new ItemStackBuilder("inventories.character-selector.help")
				.setPlaceholderObject(gamePlayer)
				.build()));
		contents.set(1, 0, ClickableItem.empty(new ItemStackBuilder("inventories.character-selector.monsters")
				.setPlaceholderObject(gamePlayer)
				.build()));
		contents.set(2, 0, ClickableItem.of(new ItemStackBuilder("inventories.character-selector.survivors")
				.setPlaceholderObject(gamePlayer)
				.build(), event -> {
					new SurvivorMenu().open(player);
					new SoundPlayer("click").playTo(player);
				}));
		Pagination pagination = contents.pagination();
		ClickableItem[] items = characterManager.getCharactersByRole(Role.MONSTER).stream()
				.map(gameCharacter -> {
					ItemStack itemstack = gameCharacter.getIcon()
							.setPlaceholderObject(gamePlayer)
							.glowingIf(game.hasSelected(gamePlayer, gameCharacter))
							.withAdditionalLores(new ListMessageBuilder(false, "additional-lore")
									.setPlaceholderObject(gamePlayer)
									.fromConfiguration(characters)
									.get())
							.build();
					return ClickableItem.of(itemstack, event -> {
							if (event.isLeftClick()) {
								if (game.isSelected(gameCharacter) && duplicates) {
									new MessageBuilder("lobby.already-selected")
											.setPlaceholderObject(game)
											.send(player);
									new SoundPlayer("error").playTo(player);
									return;
								}
								game.setSelection(gamePlayer, gameCharacter);
								new MessageBuilder("lobby.select-character")
										.replace("%character%", gameCharacter.getNode())
										.setPlaceholderObject(game)
										.send(player);
								gameCharacter.getEquipSound().playTo(player);
								open(player);
							} else {
								game.setSelection(gamePlayer, gameCharacter);
								new SoundPlayer("click").playTo(player);
								new CharacterEditor().open(player);
							}
						});
				})
				.toArray(size -> new ClickableItem[size]);
		pagination.setItems(items);
		pagination.setItemsPerPage(20);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 3).allowOverride(false));
		contents.set(5, 4, ClickableItem.of(new ItemStackBuilder("previous").build(),
				e -> inventory.open(player, pagination.previous().getPage())));
		contents.set(5, 5, ClickableItem.of(new ItemStackBuilder("close").build(),
				e -> e.getWhoClicked().closeInventory()));
		contents.set(5, 6, ClickableItem.of(new ItemStackBuilder("next").build(),
				e -> inventory.open(player, pagination.next().getPage())));
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	public void open(Player player) {
		inventory.open(player);
	}

}
