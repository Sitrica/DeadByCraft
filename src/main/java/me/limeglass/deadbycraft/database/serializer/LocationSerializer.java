package me.limeglass.deadbycraft.database.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.deadbycraft.database.Serializer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class LocationSerializer implements Serializer<Location> {

	@Override
	public JsonElement serialize(Location location, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (location == null)
			return json;
		json.addProperty("world", location.getWorld().getName());
		json.addProperty("pitch", location.getPitch());
		json.addProperty("yaw", location.getYaw());
		json.addProperty("x", location.getX());
		json.addProperty("y", location.getY());
		json.addProperty("z", location.getZ());
		return json;
	}

	@Override
	public Location deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement worldElement = object.get("world");
		if (worldElement == null)
			return null;
		World world = Bukkit.getWorld(worldElement.getAsString());
		if (world == null)
			return null;
		double x = object.get("x").getAsDouble();
		double y = object.get("y").getAsDouble();
		double z = object.get("z").getAsDouble();
		float pitch = object.get("pitch") == null ? 0.0F : object.get("pitch").getAsFloat();
		float yaw = object.get("yaw") == null ? 0.0F : object.get("yaw").getAsFloat();
		return new Location(world, x, y, z, pitch, yaw);
	}

}
