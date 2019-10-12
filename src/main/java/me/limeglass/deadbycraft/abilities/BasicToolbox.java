package me.limeglass.deadbycraft.abilities;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;
import me.limeglass.deadbycraft.objects.Game;
import me.limeglass.deadbycraft.objects.Game.Role;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.objects.Toolbox;
import me.limeglass.deadbycraft.utils.HologramBuilder;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class BasicToolbox extends Ability implements Listener {

	public ItemStackBuilder toolbox;
	private int amount;

	public BasicToolbox() {
		super(Role.SURVIVOR, Material.CHEST, "Basic Toolbox", "&7Placing a toolbox near a generator", "&7will increase repair speeds. The toolbox can", "&7be broken by monsters, and you only get one.", "&ePlace the given item near a generator.");
		Bukkit.getPluginManager().registerEvents(this, DeadByCraft.getInstance());
	}

	@Override
	public void initalizeConfiguration(FileConfiguration configuration) {
		if (!configuration.isSet("cooldown-seconds"))
			configuration.set("cooldown-seconds", "2 minutes");
		if (!configuration.isSet("lasting-seconds"))
			configuration.set("lasting-seconds", "10 seconds");
		if (!configuration.isSet("amount"))
			configuration.set("amount", 1);
		if (!configuration.isConfigurationSection("toolbox")) {
			configuration.set("toolbox.material", "CHEST");
			configuration.set("toolbox.title", "&e&l%player%'s Toolbox");
			configuration.set("toolbox.lore", new String[] {"&7Place on the ground near a generator", "&7to improve repair times."});
		}
		toolbox = new ItemStackBuilder("toolbox").fromConfiguration(configuration);
		amount = configuration.getInt("amount", 1);
	}

	@Override
	public void tick(Game game, GamePlayer gamePlayer) {}

	@Override
	public void onGameStart(Game game, GamePlayer gamePlayer) {
		Optional<Player> player = gamePlayer.getPlayer();
		if (!player.isPresent())
			return;
		player.get().getInventory().setItem(4, getToolbox(gamePlayer, game));
	}

	private ItemStack getToolbox(GamePlayer gamePlayer, Game game) {
		Optional<Player> player = gamePlayer.getPlayer();
		if (!player.isPresent())
			return null;
		ItemStack itemstack = toolbox
				.replace("%player%", player.get().getName())
				.setPlaceholderObject(game)
				.build();
		itemstack.setAmount(amount);
		return itemstack;
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = DeadByCraft.getInstance().getManager(PlayerManager.class).getGamePlayer(player);
		Optional<Game> optional = gamePlayer.getCurrentGame();
		if (!optional.isPresent())
			return;
		Game game = optional.get();
		if (event.getItemInHand().isSimilar(getToolbox(gamePlayer, game))) {
			Block block = event.getBlock();
			new MessageBuilder("abilities.toolbox-placed")
					.replace("%player%", player.getName())
					.setPlaceholderObject(game)
					.send(player);
			HologramBuilder builder = new HologramBuilder(block.getLocation().add(0, 1, 0), "d")
					.toPlayers(game.getBukkitPlayers())
					.setPlaceholderObject(game);
			long expiration = builder.getExpiration();
			game.addToolbox(new Toolbox(block, gamePlayer, expiration));
			builder.send();
		}
	}

	@Override
	public void onGameLeave(GamePlayer gamePlayer) {}

}
