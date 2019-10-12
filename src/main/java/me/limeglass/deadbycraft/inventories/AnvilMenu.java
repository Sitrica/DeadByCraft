package me.limeglass.deadbycraft.inventories;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.limeglass.deadbycraft.DeadByCraft;
import net.wesjd.anvilgui.version.VersionMatcher;
import net.wesjd.anvilgui.version.VersionWrapper;

/**
 * An anvil gui, used for gathering a user's input.
 * Heavily optimized and made to allow for itemstack customization.
 * @author Wesley Smith and LimeGlass
 */
public class AnvilMenu {

	private static VersionWrapper WRAPPER = new VersionMatcher().match();
	private final ListenUp listener = new ListenUp();
	private final Consumer<String> consumer;
	private final Inventory inventory;
	private final int containerId;
	private final Player holder;
	private boolean open;

	public AnvilMenu(ItemStack searchItem, Player holder, Consumer<String> consumer) {

		this.consumer = consumer;
		this.holder = holder;

		WRAPPER.handleInventoryCloseEvent(holder);
		WRAPPER.setActiveContainerDefault(holder);

		Bukkit.getPluginManager().registerEvents(listener, DeadByCraft.getInstance());

		Object container = WRAPPER.newContainerAnvil(holder);

		inventory = WRAPPER.toBukkitInventory(container);
		inventory.setItem(0, searchItem);

		containerId = WRAPPER.getNextContainerId(holder);
		WRAPPER.sendPacketOpenWindow(holder, containerId);
		WRAPPER.setActiveContainer(holder, container);
		WRAPPER.setActiveContainerId(container, containerId);
		WRAPPER.addActiveContainerSlotListener(container, holder);
		
		open = true;
	}

	private class ListenUp implements Listener {

		@EventHandler
		public void onInventoryClick(InventoryClickEvent event) {
			if (!event.getInventory().equals(inventory))
				return;
			event.setCancelled(true);
			if (event.getRawSlot() != 2)
				return;
			 ItemStack clicked = inventory.getItem(2);
             if (clicked == null || clicked.getType() == Material.AIR)
            	 return;
            consumer.accept(clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString());
			closeInventory();
		}

		@EventHandler
		public void onInventoryClose(InventoryCloseEvent event) {
			if (event.getInventory().equals(inventory))
				closeInventory();
		}

	}

	public void closeInventory() {
		if (!open)
			return;
		open = false;
		
		WRAPPER.handleInventoryCloseEvent(holder);
		WRAPPER.setActiveContainerDefault(holder);
		WRAPPER.sendPacketCloseWindow(holder, containerId);
		HandlerList.unregisterAll(listener);
	}

	public Inventory getInventory() {
		return inventory;
	}

}
