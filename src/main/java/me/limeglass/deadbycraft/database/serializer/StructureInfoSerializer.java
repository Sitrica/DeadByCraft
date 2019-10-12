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
import me.limeglass.deadbycraft.manager.managers.StructureManager.StructureInfo;
import me.limeglass.deadbycraft.objects.BlockInfo;

public class StructureInfoSerializer implements Serializer<StructureInfo> {

	@Override
	public JsonElement serialize(StructureInfo info, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject element = new JsonObject();
		element.addProperty("name", info.getName());
		element.add("origin", context.serialize(info.getOrigin(), Location.class));
		JsonArray blocks = new JsonArray();
		info.getBlocks().forEach(block -> blocks.add(context.serialize(block, BlockInfo.class)));
		element.add("blocks", blocks);
		return element;
	}

	@Override
	public StructureInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement nameElement = object.get("name");
		if (nameElement == null)
			return null;
		JsonElement originElement = object.get("origin");
		if (originElement == null)
			return null;
		Location origin = context.deserialize(originElement, Location.class);
		List<BlockInfo> blocks = new ArrayList<>();
		JsonElement blocksElement = object.get("blocks");
		if (blocksElement != null && !blocksElement.isJsonNull() && blocksElement.isJsonArray()) {
			JsonArray array = blocksElement.getAsJsonArray();
			array.forEach(element -> {
				BlockInfo info = context.deserialize(element, BlockInfo.class);
				blocks.add(info);
			});
		}
		return new StructureInfo(nameElement.getAsString(), origin, blocks);
	}

}
