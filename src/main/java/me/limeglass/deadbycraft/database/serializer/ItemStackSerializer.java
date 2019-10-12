package me.limeglass.deadbycraft.database.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.deadbycraft.database.Serializer;
import me.limeglass.deadbycraft.database.Utf8YamlConfiguration;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

public class ItemStackSerializer implements Serializer<ItemStack> {

	@Override
	public JsonElement serialize(ItemStack item, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		String serialized = null;
		try {
			FileConfiguration fc = new Utf8YamlConfiguration();
			fc.set("ItemStack", item);
			serialized = fc.saveToString();
		} finally {
			if (serialized == null)
				serialized = "";
		}
		object.addProperty("ItemStack", serialized);
		return object;
	}

	@Override
	public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		FileConfiguration configuration = new Utf8YamlConfiguration();
		try {
			String serialized = object.get("ItemStack").getAsString();
			configuration.loadFromString(serialized);
			return configuration.getItemStack("ItemStack", new ItemStack(Material.AIR));
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

}
