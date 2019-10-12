package me.limeglass.deadbycraft.inventories;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.abilities.Ability;
import me.limeglass.deadbycraft.manager.managers.CharacterManager;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.AbilityConfiguration;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;
import me.limeglass.deadbycraft.manager.managers.GameMechanicManager;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.objects.Loadout;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.ListMessageBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.SoundPlayer;

public class CharacterEditor implements InventoryProvider {

	private final CharacterManager characterManager;
	private final FileConfiguration inventories;
	private final PlayerManager playerManager;
	private final SmartInventory inventory;

	public CharacterEditor() {
		DeadByCraft instance = DeadByCraft.getInstance();
		playerManager = instance.getManager(PlayerManager.class);
		characterManager = instance.getManager(CharacterManager.class);
		inventories = instance.getConfiguration("inventories").get();
		inventory =  SmartInventory.builder()
				.title(new MessageBuilder(false, "inventories.editor.title")
						.fromConfiguration(inventories)
						.get())
				.manager(DeadByCraft.getInventoryManager())
				.provider(this)
				.id("editor")
				.size(6, 9)
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		GamePlayer gamePlayer = playerManager.getGamePlayer(player);
		contents.fillBorders(ClickableItem.empty(new ItemStackBuilder("inventories.editor.border")
				.setPlaceholderObject(gamePlayer)
				.build()));
		contents.fillRect(0, 1, 2, 7, ClickableItem.empty(new ItemStackBuilder("inventories.editor.abilities-border")
				.setPlaceholderObject(gamePlayer)
				.build()));
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		Pagination pagination = contents.pagination();
		Optional<GameCharacter> character = game.getSelection(gamePlayer);
		if (!character.isPresent())
			return;
		ClickableItem[] items = characterManager.getAbilitiesFor(character.get()).stream()
				.map(ability -> {
					AbilityConfiguration abilityConfiguration = characterManager.getAbilityConfiguration(ability);
					ItemStack itemstack = abilityConfiguration.getIcon()
							.setPlaceholderObject(gamePlayer)
							.withAdditionalLores(new ListMessageBuilder(false, "abilities.additional-lore")
									.setPlaceholderObject(ability)
									.get())
							.build();
					return ClickableItem.of(itemstack, event -> {
							contents.setProperty("selected", ability.getName());
							ItemStack item = event.getCurrentItem();
							if (item != null) {
								ItemMeta meta = item.getItemMeta();
								meta.addEnchant(Enchantment.DURABILITY, 1, true);
								item.setItemMeta(meta);
							}
							new MessageBuilder("abilities.selected")
									.replace("%ability%", ability.getName())
									.setPlaceholderObject(game)
									.send(player);
							new SoundPlayer("click").playTo(player);
						});
				})
				.toArray(size -> new ClickableItem[size]);
		pagination.setItems(items);
		pagination.setItemsPerPage(14);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 3, 1).allowOverride(false));

		Loadout loadout = gamePlayer.getCharacterLoadout(character.get());
		for (int slot = 0; slot < 5; slot++) {
			int column = 2 + slot;
			int required = characterManager.getSlotLevel(slot);
			if (required < 0) {
				contents.set(1, column, ClickableItem.empty(new ItemStackBuilder("inventories.editor.disabled")
						.setPlaceholderObject(game)
						.build()));
				continue;
			}
			if (!characterManager.isSlotUnlocked(slot, gamePlayer)) {
				contents.set(1, column, ClickableItem.empty(new ItemStackBuilder("inventories.editor.not-unlocked")
						.replace("%level%", required)
						.setPlaceholderObject(game)
						.build()));
				continue;
			} else {
				Optional<Ability> ability = loadout.getAbilitySlot(slot);
				int selectedSlot = slot;
				if (!ability.isPresent()) {
					contents.set(1, column, ClickableItem.of(new ItemStackBuilder("inventories.editor.not-used")
							.replace("%level%", required)
							.setPlaceholderObject(game)
							.build(), event -> {
								String property = contents.property("selected");
								if (property == null)
									return;
								characterManager.getAbility(property).ifPresent(selected -> {
										contents.setProperty("selected", null);
										loadout.setAbilitySlot(selectedSlot, selected);
										new SoundPlayer("ability-equipped").playTo(player);
										new MessageBuilder("abilities.equipped")
												.replace("%ability%", selected.getName())
												.replace("%slot%", selectedSlot + 1)
												.setPlaceholderObject(game)
												.send(player);
								});
								open(player);
							}));
					continue;
				} else {
					AbilityConfiguration abilityConfiguration = characterManager.getAbilityConfiguration(ability.get());
					ItemStack item = abilityConfiguration.getIcon()
							.replace("%level%", required)
							.setPlaceholderObject(game)
							.withAdditionalLores(new ListMessageBuilder(false, "abilities.remove-lore")
									.setPlaceholderObject(ability)
									.get())
							.build();
					contents.set(1, column, ClickableItem.of(item, event -> {
							String property = contents.property("selected");
							if (property != null) {
								characterManager.getAbility(property).ifPresent(selected -> {
									contents.setProperty("selected", null);
									loadout.setAbilitySlot(selectedSlot, selected);
									new SoundPlayer("ability-equipped").playTo(player);
									new MessageBuilder("abilities.equipped")
											.replace("%ability%", selected.getName())
											.replace("%slot%", selectedSlot + 1)
											.setPlaceholderObject(game)
											.send(player);
								});
							} else {
								loadout.removeAbilitySlot(selectedSlot);
								new SoundPlayer("ability-removed").playTo(player);
								new MessageBuilder("abilities.removed")
										.replace("%ability%", abilityConfiguration.getName())
										.replace("%slot%", selectedSlot + 1)
										.setPlaceholderObject(game)
										.send(player);
							}
							open(player);
						}));
				}
			}
		}
		contents.set(0, 0, ClickableItem.empty(new ItemStackBuilder("inventories.editor.help")
				.setPlaceholderObject(gamePlayer)
				.build()));
		contents.set(0, 8, ClickableItem.empty(new ItemStackBuilder("inventories.editor.levels")
				.setPlaceholderObject(gamePlayer)
				.build()));
		contents.set(5, 3, ClickableItem.of(new ItemStackBuilder("previous").build(),
				e -> inventory.open(player, pagination.previous().getPage())));
		contents.set(5, 4, ClickableItem.of(new ItemStackBuilder("inventories.editor.back")
				.setPlaceholderObject(gamePlayer)
				.build(), e -> DeadByCraft.getInstance().getManager(GameMechanicManager.class).openCurrentMenu(gamePlayer)));
		contents.set(5, 5, ClickableItem.of(new ItemStackBuilder("next").build(),
				e -> inventory.open(player, pagination.next().getPage())));
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	public void open(Player player) {
		inventory.open(player);
	}

}
