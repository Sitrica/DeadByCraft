package me.limeglass.deadbycraft.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.google.common.collect.Sets;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.external.HolographicDisplaysManager;
import me.limeglass.deadbycraft.placeholders.Placeholder;
import me.limeglass.deadbycraft.placeholders.Placeholders;
import me.limeglass.deadbycraft.placeholders.SimplePlaceholder;

public class HologramBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private final Set<Player> players = new HashSet<>();
	private final HolographicDisplaysManager holograms;
	private final FileConfiguration configuration;
	private Object defaultPlaceholderObject;
	private final Location location;
	private long defaultExpiration;
	private final String node;
	private boolean update;

	/**
	 * Creates a HologramBuilder with the defined nodes..
	 * 
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public HologramBuilder(Location location, String node) {
		DeadByCraft instance = DeadByCraft.getInstance();
		this.holograms = instance.getExternalManager(HolographicDisplaysManager.class);
		this.configuration = instance.getConfig();
		this.location = location;
		this.node = node + ".";
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Players... to send the message to.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder toPlayers(Player... players) {
		this.players.addAll(Sets.newHashSet(players));
		return this;
	}

	/**
	 * Set the hologram to disappear after this many ticks.
	 *
	 * @param ticks The amount of ticks to wait before removing hologram.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder withDefaultExpiration(int ticks) {
		this.defaultExpiration = ticks;
		return this;
	}

	/**
	 * Set the hologram to disappear after this many ticks.
	 *
	 * @param ticks The amount of ticks to wait before removing hologram.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder withDefaultExpiration(String interval) {
		this.defaultExpiration = IntervalUtils.getInterval(interval);
		return this;
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Collection<Player> to send the message to.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder toPlayers(Collection<? extends Player> players) {
		this.players.addAll(players);
		return this;
	}

	/**
	 * Add a placeholder to the HologramBuilder.
	 * 
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
		this.defaultPlaceholderObject = placeholderObject;
		placeholders.put(placeholder, placeholderObject);
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object.
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Replace a placeholder every update the hologram makes. The function replacer is the time remaining.
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param function Function<Object, String> to execute on update, return a String here.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder updatableReplace(String syntax, Function<Long, String> function) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return function.apply(defaultExpiration);
			}
		}, defaultExpiration);
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The MessageBuilder for chaining.
	 */
	public HologramBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}

	/**
	 * Set if the hologram should constantly update until expiration.
	 * 
	 * @param update boolean if it should update.
	 * @return The HologramBuilder for chaining.
	 */
	public HologramBuilder update(boolean update) {
		this.update = update;
		return this;
	}

	/**
	 * Sends the final product of the builder.
	 */
	public void send(Player... players) {
		toPlayers(players).send();
	}

	private String applyPlaceholders(String input) {
		// Default Placeholders
		placeholders.put(new SimplePlaceholder("%expiration%") {
			@Override
			public String get() {
				return getExpiration() + "";
			}
		}, getExpiration() + "");
		for (Placeholder<?> placeholder : Placeholders.getPlaceholders()) {
			for (String syntax : placeholder.getSyntaxes()) {
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else if (defaultPlaceholderObject != null && placeholder.getType().isAssignableFrom(defaultPlaceholderObject.getClass())) {
					String replacement = placeholder.replace_i(defaultPlaceholderObject);
					if (replacement != null)
						input = input.replaceAll(Pattern.quote(syntax), replacement);
				}
			}
		}
		// Registered Placeholders
		for (Entry<Placeholder<?>, Object> entry : placeholders.entrySet()) {
			Placeholder<?> placeholder = entry.getKey();
			for (String syntax : placeholder.getSyntaxes()) {
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else {
					String replacement = placeholder.replace_i(entry.getValue());
					if (replacement != null)
						input = input.replaceAll(Pattern.quote(syntax), replacement);
				}
			}
		}
		return input;
	}

	public long getExpiration() {
		String expirationString = configuration.getString(node + "expiration", "30 seconds");
		long expiration = IntervalUtils.getInterval(expirationString);
		if (expiration <= 0)
			expiration = defaultExpiration;
		return expiration;
	}

	/**
	 * Sends the final product of the builder if the senders are set.
	 */
	public void send() {
		if (!holograms.isEnabled())
			return;
		if (players.isEmpty())
			return;
		double x = configuration.getDouble(node + "x-offset", 0);
		double y = configuration.getDouble(node + "y-offset", 0);
		double z = configuration.getDouble(node + "z-offset", 0);
		long expiration = getExpiration();
		Hologram hologram = holograms.createHologram(location.add(x, y, z));
		VisibilityManager visibilityManager = hologram.getVisibilityManager();
		visibilityManager.setVisibleByDefault(false);
		visibilityManager.resetVisibilityAll();
		boolean above = configuration.getBoolean(node + "item.above", false);
		boolean item = configuration.getBoolean(node + "item.enabled", false);
		Material material = Utils.materialAttempt(configuration.getString(node + "item.material", "DIAMOND_SWORD"), "DIAMOND_SWORD");
		ItemStack itemstack = new ItemStack(material);
		itemstack.setItemMeta(DeprecationUtils.setupItemMeta(itemstack.getItemMeta(), configuration.getString(node + "item.material-meta", "")));
		if (above && item)
			hologram.appendItemLine(itemstack);
		for (String string : configuration.getStringList(node + "lines")) {
			String complete = Formatting.color(string);
			complete = applyPlaceholders(complete);
			hologram.appendTextLine(complete);
		}
		if (!above && item)
			hologram.appendItemLine(itemstack);
		boolean sound = configuration.getBoolean(node + "use-sounds", false);
		SoundPlayer soundPlayer = new SoundPlayer(configuration.getConfigurationSection(node + "sounds"));
		for (Player player : players) {
			visibilityManager.showTo(player);
			if (sound)
				soundPlayer.playTo(player);
		}
		DeadByCraft instance = DeadByCraft.getInstance();
		if (update) {
			for (int i = 0; i <= expiration; i++) {
				Bukkit.getServer().getScheduler().runTaskLater(instance, () -> update(hologram), i);
				defaultExpiration--;
			}
		}
		// Might not account for shutdown during expiration, but it's using the API and not saving over restart.
		instance.getServer().getScheduler().runTaskLater(instance, () -> hologram.delete(), expiration);
	}

	public void update(Hologram hologram) {
		boolean above = configuration.getBoolean(node + "item.above", false);
		boolean item = configuration.getBoolean(node + "item.enabled", false);
		Material material = Utils.materialAttempt(configuration.getString(node + "item.material", "DIAMOND_SWORD"), "DIAMOND_SWORD");
		ItemStack itemstack = new ItemStack(material);
		itemstack.setItemMeta(DeprecationUtils.setupItemMeta(itemstack.getItemMeta(), configuration.getString(node + "item.material-meta", "")));
		if (above && item) {
			HologramLine line = hologram.getLine(0);
			if (!(line instanceof ItemLine))
				return;
			ItemLine itemLine = (ItemLine) line;
			itemLine.setItemStack(itemstack);
		}
		int i = 0;
		if (above)
			i++;
		for (String string : configuration.getStringList(node + "lines")) {
			String complete = Formatting.color(string);
			complete = applyPlaceholders(complete);
			HologramLine line = hologram.getLine(i);
			if (!(line instanceof TextLine))
				continue;
			TextLine textLine = (TextLine) line;
			textLine.setText(complete);
		}
		if (!above && item) {
			HologramLine line = hologram.getLine(hologram.size());
			if (!(line instanceof ItemLine))
				return;
			ItemLine itemLine = (ItemLine) line;
			itemLine.setItemStack(itemstack);
		}
	}

}
