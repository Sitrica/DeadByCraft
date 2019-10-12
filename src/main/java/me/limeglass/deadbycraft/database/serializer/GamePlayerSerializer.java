package me.limeglass.deadbycraft.database.serializer;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.deadbycraft.database.Serializer;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.objects.Loadout;

public class GamePlayerSerializer implements Serializer<GamePlayer> {

	@Override
	public JsonElement serialize(GamePlayer player, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject element = new JsonObject();
		element.addProperty("uuid", player.getUniqueId() + "");
		element.addProperty("experience", player.getExperience());
		element.addProperty("level", player.getLevel());
		JsonArray loadouts = new JsonArray();
		player.getLoadouts().forEach(loadout -> loadouts.add(context.serialize(loadout, Loadout.class)));
		element.add("loadouts", loadouts);
		return element;
	}

	@Override
	public GamePlayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		String stringUUID = object.get("uuid").getAsString();
		if (stringUUID == null)
			return null;
		UUID uuid = UUID.fromString(stringUUID);
		if (uuid == null)
			return null;
		GamePlayer player = new GamePlayer(uuid);
		JsonElement levelElement = object.get("level");
		if (levelElement != null)
			player.setLevel(levelElement.getAsInt());
		JsonElement experienceElement = object.get("experience");
		if (experienceElement != null)
			player.setExperience(experienceElement.getAsInt());
		JsonElement loadoutsElement = object.get("loadouts");
		if (loadoutsElement != null && !loadoutsElement.isJsonNull() && loadoutsElement.isJsonArray()) {
			JsonArray array = loadoutsElement.getAsJsonArray();
			array.forEach(element -> {
				Loadout loadout = context.deserialize(element, Loadout.class);
				if (loadout == null)
					return;
				player.addLoadout(loadout);
			});
		}
		return player;
	}

}
