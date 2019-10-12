package me.limeglass.deadbycraft.database.serializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.deadbycraft.database.Serializer;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.BlockInfo;
import me.limeglass.deadbycraft.utils.CuboidRegion;

public class ArenaInfoSerializer implements Serializer<ArenaInfo> {

	@Override
	public JsonElement serialize(ArenaInfo info, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (info == null)
			return json;
		CuboidRegion region = info.getRegion();
		json.add("pos1", context.serialize(region.getPosition1(), Location.class));
		json.add("pos2", context.serialize(region.getPosition2(), Location.class));
		json.add("lobby", context.serialize(info.getLobby(), Location.class));
		json.addProperty("name", info.getName());
		json.addProperty("max", info.getMaximumPlayers());
		json.addProperty("min", info.getMinimumPlayers());
		JsonArray blocks = new JsonArray();
		info.getSavedBlocks().forEach(block -> blocks.add(context.serialize(block, BlockInfo.class)));
		json.add("blocks", blocks);
		JsonArray gates = new JsonArray();
		info.getGates().forEach(gate -> gates.add(context.serialize(gate, BlockInfo.class)));
		json.add("gates", gates);
		JsonArray spawns = new JsonArray();
		info.getSpawns().forEach(spawn -> spawns.add(context.serialize(spawn, Location.class)));
		json.add("spawns", spawns);
		JsonArray levers = new JsonArray();
		info.getLevers().forEach(lever -> levers.add(context.serialize(lever, Location.class)));
		json.add("levers", levers);
		JsonArray generators = new JsonArray();
		info.getGenerators().forEach(generator -> generators.add(context.serialize(generator, Location.class)));
		json.add("generators", generators);
		return json;
	}

	@Override
	public ArenaInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement nameElement = object.get("name");
		if (nameElement == null)
			return null;
		JsonElement pos1Element = object.get("pos1");
		if (pos1Element == null)
			return null;
		Location pos1 = context.deserialize(pos1Element, Location.class);
		JsonElement pos2Element = object.get("pos2");
		if (pos2Element == null)
			return null;
		Location pos2 = context.deserialize(pos2Element, Location.class);
		JsonElement lobbyElement = object.get("lobby");
		if (lobbyElement == null)
			return null;
		Location lobby = context.deserialize(lobbyElement, Location.class);
		ArenaInfo info = new ArenaInfo(nameElement.getAsString(), new CuboidRegion(pos1, pos2), lobby);
		JsonElement minElement = object.get("min");
		if (minElement != null)
			info.setMinimumPlayers(minElement.getAsInt());
		JsonElement maxElement = object.get("max");
		if (maxElement != null)
			info.setMaximumPlayers(maxElement.getAsInt());
		JsonElement blocksElement = object.get("blocks");
		if (blocksElement != null && !blocksElement.isJsonNull() && blocksElement.isJsonArray()) {
			JsonArray array = blocksElement.getAsJsonArray();
			array.forEach(element -> {
				BlockInfo block = context.deserialize(element, BlockInfo.class);
				if (block == null)
					return;
				info.getSavedBlocks().add(block);
			});
		}
		JsonElement gatesElement = object.get("gates");
		List<BlockInfo> gates = new ArrayList<>();
		if (gatesElement != null && !gatesElement.isJsonNull() && gatesElement.isJsonArray()) {
			JsonArray array = gatesElement.getAsJsonArray();
			array.forEach(element -> {
				BlockInfo gate = context.deserialize(element, BlockInfo.class);
				if (gate == null)
					return;
				gates.add(gate);
			});
		}
		info.setGates(gates);
		JsonElement spawnsElement = object.get("spawns");
		if (spawnsElement != null && !spawnsElement.isJsonNull() && spawnsElement.isJsonArray()) {
			JsonArray array = spawnsElement.getAsJsonArray();
			array.forEach(element -> {
				Location spawn = context.deserialize(element, Location.class);
				if (spawn == null)
					return;
				info.addSpawn(spawn);
			});
		}
		JsonElement leversElement = object.get("levers");
		if (leversElement != null && !leversElement.isJsonNull() && leversElement.isJsonArray()) {
			JsonArray array = leversElement.getAsJsonArray();
			array.forEach(element -> {
				Location lever = context.deserialize(element, Location.class);
				if (lever == null)
					return;
				info.addLever(lever);
			});
		}
		JsonElement generatorsElement = object.get("generators");
		if (generatorsElement != null && !generatorsElement.isJsonNull() && generatorsElement.isJsonArray()) {
			JsonArray array = generatorsElement.getAsJsonArray();
			array.forEach(element -> {
				Location generator = context.deserialize(element, Location.class);
				if (generator == null)
					return;
				info.addGenerator(generator);
			});
		}
		return info;
	}

}
