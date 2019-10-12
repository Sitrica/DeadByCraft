package me.limeglass.deadbycraft.manager.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.database.serializer.BlockInfoSerializer;
import me.limeglass.deadbycraft.database.serializer.LocationSerializer;
import me.limeglass.deadbycraft.database.serializer.StructureInfoSerializer;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.objects.BlockInfo;
import me.limeglass.deadbycraft.utils.Compression;
import me.limeglass.deadbycraft.utils.CuboidRegion;
import me.limeglass.deadbycraft.utils.IntervalUtils;
import me.limeglass.deadbycraft.utils.ItemStackBuilder;
import me.limeglass.deadbycraft.utils.MessageBuilder;
import me.limeglass.deadbycraft.utils.VectorUtil;

public class StructureManager extends Manager {

	private final Map<Player, Location> position1 = new HashMap<>(), position2 = new HashMap<>();
	private final Map<String, StructureInfo> structures = new HashMap<>();
	private final Gson gson = new GsonBuilder()
			.registerTypeAdapter(StructureInfo.class, new StructureInfoSerializer())
			.registerTypeAdapter(BlockInfo.class, new BlockInfoSerializer())
			.registerTypeAdapter(Location.class, new LocationSerializer())
			.enableComplexMapKeySerialization()
			.serializeNulls()
			.create();
	private final File FOLDER, generatorFile;
	private StructureInfo generator;

	public StructureManager() {
		super(true);
		DeadByCraft instance = DeadByCraft.getInstance();
		FOLDER = new File(instance.getDataFolder() + "/structures");
		if (!FOLDER.exists())
			FOLDER.mkdirs();
		generatorFile = new File(FOLDER, "generator.structure");
		if (!generatorFile.exists()) {
			InputStream resource = instance.getResource("generator.structure");
			if (resource == null)
				return;
			try (InputStream in = resource) {
				Files.copy(in, generatorFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				DeadByCraft.consoleMessage("There was a problem copying the default generator structure.");
				return;
			}
		}
		try {
			generator = loadStructure(generatorFile);
		} catch (IllegalAccessException | IOException e) {
			e.printStackTrace();
			DeadByCraft.consoleMessage("There was a problem copying the default generator structure.");
			return;
		}

		structures.put("generator", generator);

		// Cache removal.
		long ticks = IntervalUtils.getInterval("15 minutes");
		Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
			Iterator<Entry<String, StructureInfo>> iterator = structures.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, StructureInfo> entry = iterator.next();
				File file = new File(FOLDER, entry.getKey() + ".structure");
				if (!file.exists())
					iterator.remove();
			}
		}, ticks, ticks);
	}

	public static class StructureInfo {

		public final List<BlockInfo> blocks = new ArrayList<>();
		private final Location origin;
		private final String name;

		public StructureInfo(String name, Location origin, Collection<BlockInfo> blocks) {
			this.blocks.addAll(blocks);
			this.origin = origin;
			this.name = name;
		}

		public Location getOrigin() {
			return origin;
		}

		public List<BlockInfo> getBlocks() {
			return blocks;
		}

		public String getName() {
			return name;
		}

	}

	@Nullable
	public StructureInfo getGenerator() {
		return Optional.ofNullable(generator).orElseGet(() -> {
			if (!generatorFile.exists()) {
				try (InputStream in = DeadByCraft.getInstance().getResource("generator.structure")) {
					Files.copy(in, generatorFile.toPath());
				} catch (IOException e) {
					e.printStackTrace();
					DeadByCraft.consoleMessage("There was a problem copying the default generator structure.");
					return null;
				}
			}
			try {
				return loadStructure(generatorFile);
			} catch (IllegalAccessException | IOException e) {
				e.printStackTrace();
				DeadByCraft.consoleMessage("There was a problem copying the default generator structure.");
			}
			return generator;
		});
	}

	public Optional<StructureInfo> getStructure(String name) {
		return Optional.ofNullable(structures.entrySet().stream()
				.filter(entry -> entry.getKey().equalsIgnoreCase(name))
				.map(entry -> entry.getValue())
				.findFirst()
				.orElseGet(() -> {
					File file = new File(FOLDER, name + ".structure");
					if (!file.exists())
						return null;
					try {
						StructureInfo info = loadStructure(file);
						structures.put(name, info);
						return info;
					} catch (IllegalAccessException | IOException e) {
						DeadByCraft.debugMessage("There was a problem loading structure '" + name + "'");
					}
					return null;
				}));
	}

	public boolean saveStructure(String name, Location origin, Player player) throws IOException {
		File file = new File(FOLDER, name + ".structure");
		Location pos1 = position1.get(player);
		Location pos2 = position2.get(player);
		if (pos1 == null || pos2 == null)
			return false;
		saveStructure(file, origin, new CuboidRegion(pos1, pos2));
		return true;
	}

	public void saveStructure(String name, Location origin, CuboidRegion region) throws IOException {
		File file = new File(FOLDER, name + ".structure");
		saveStructure(file, origin, region);
	}

	public void saveStructure(File file, Location origin, CuboidRegion region) throws IOException {
		if (!file.exists())
			file.createNewFile();

		List<BlockInfo> blocks = Lists.newArrayList(region.iterator()).stream()
				.filter(block -> block.getType() != Material.AIR)
				.map(block -> new BlockInfo(block))
				.collect(Collectors.toList());

		StructureInfo info = new StructureInfo(file.getName(), origin, blocks);
		FileOutputStream fileOutputStream = new FileOutputStream(file, false);
		fileOutputStream.write(Compression.compress(gson.toJson(info)));
		fileOutputStream.flush();
		fileOutputStream.close();
	}

	public StructureInfo loadStructure(File file) throws IOException, IllegalAccessException {
		if (!file.exists())
			throw new IllegalAccessException("File requested does not exist");

		byte[] content = new byte[(int) file.length()];

		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.read(content);
		fileInputStream.close();

		String json = Compression.decompress(content);
		return gson.fromJson(json, StructureInfo.class);
	}

	public void pasteStructure(StructureInfo info, Location location) {
		pasteStructure(info, location, RotationDegrees.NONE);
	}

	public void pasteStructure(StructureInfo info, Location location, RotationDegrees rotation) {
		Validate.notNull(info);
		DeadByCraft instance = DeadByCraft.getInstance();
		Location origin = info.getOrigin();
		for (BlockInfo block : info.getBlocks()) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> {
				Location rotated = rotateLocation(new Location(location.getWorld(), block.getX(), block.getY(), block.getZ()), rotation);
				Location blockLocation = new Location(location.getWorld(),
						location.getX() - origin.getBlockX(),
						location.getY() - origin.getBlockY(),
						location.getZ() - origin.getBlockZ());
				blockLocation.add(rotated);
				Block update = blockLocation.getBlock();
				update.setType(block.getMaterial());
				update.setBlockData(block.getBlockData());
			});
		}
	}

	public enum RotationDegrees {
		NONE,
		ROTATE_90,
		ROTATE_180,
		ROTATE_270;
	}

	public Location rotateLocation(Location location, RotationDegrees rotation) {
		Vector vector = location.toVector();
		World world = location.getWorld();
		switch (rotation) {
			case ROTATE_90:
				return VectorUtil.rotateAroundAxisY(vector, 90).toLocation(world);
			case ROTATE_180:
				return VectorUtil.rotateAroundAxisY(vector, 180).toLocation(world);
			case ROTATE_270:
				return VectorUtil.rotateAroundAxisY(vector, 270).toLocation(world);
			case NONE:
			default:
				return location;
		}
    }

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (event.getHand() != EquipmentSlot.HAND)
			return;
		Player player = event.getPlayer();
		if (!Sets.newHashSet("deadbycraft.structure", "deadbycraft.structures", "deadbycraft.admin").stream()
				.anyMatch(permission -> player.hasPermission(permission)))
			return;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null)
			return;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return;
		ItemStack tool = new ItemStackBuilder("structure-tool").build();
		ItemMeta toolMeta = tool.getItemMeta();
		if (toolMeta == null)
			return;
		if (tool.getType() != item.getType())
			return;
		if (!meta.getDisplayName().equalsIgnoreCase(toolMeta.getDisplayName()))
			return;
		event.setCancelled(true);
		Location location = event.getClickedBlock().getLocation();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			position2.put(player, location);
			new MessageBuilder("commands.structures.position2")
					.replace("%location%", location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ())
					.setPlaceholderObject(player)
					.send(player);
			return;
		}
		position1.put(player, location);
		new MessageBuilder("commands.structures.position1")
				.replace("%location%", location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ())
				.setPlaceholderObject(player)
				.send(player);
	}

}
