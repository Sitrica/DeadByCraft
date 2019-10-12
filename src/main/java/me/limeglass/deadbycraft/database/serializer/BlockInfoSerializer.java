package me.limeglass.deadbycraft.database.serializer;

import java.lang.reflect.Type;

import org.bukkit.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.deadbycraft.database.Serializer;
import me.limeglass.deadbycraft.objects.BlockInfo;

public class BlockInfoSerializer implements Serializer<BlockInfo> {

	@Override
	public JsonElement serialize(BlockInfo block, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject element = new JsonObject();
		element.add("location", context.serialize(block.getLocation(), Location.class));
		element.addProperty("data", block.getBlockData().getAsString());
		element.addProperty("material", block.getMaterial().name());
		return element;
	}

	@Override
	public BlockInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement locationElement = object.get("location");
		if (locationElement == null)
			return null;
		Location location = context.deserialize(locationElement, Location.class);
		JsonElement materialElement = object.get("material");
		if (materialElement == null)
			return null;
		JsonElement dataElement = object.get("data");
		if (dataElement == null)
			return null;
		return new BlockInfo(location, materialElement.getAsString(), dataElement.getAsString());
	}

}
