package me.limeglass.deadbycraft.database.serializer;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.abilities.Ability;
import me.limeglass.deadbycraft.database.Serializer;
import me.limeglass.deadbycraft.manager.managers.CharacterManager;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;
import me.limeglass.deadbycraft.objects.Loadout;

public class LoadoutSerializer implements Serializer<Loadout> {

	@Override
	public JsonElement serialize(Loadout loadout, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject element = new JsonObject();
		element.addProperty("character", loadout.getCharacter().getNode());
		element.addProperty("uuid", loadout.getPlayerUUID() + "");
		JsonArray abilities = new JsonArray();
		loadout.getAbilities().values().forEach(ability -> abilities.add(ability.getName()));
		element.add("abilities", abilities);
		return element;
	}

	@Override
	public Loadout deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		CharacterManager characterManager = DeadByCraft.getInstance().getManager(CharacterManager.class);
		JsonElement characterElement = object.get("character");
		if (characterElement == null)
			return null;
		Optional<GameCharacter> character = characterManager.getCharacter(characterElement.getAsString());
		// Doesn't exist anymore.
		if (!character.isPresent())
			return null;
		String stringUUID = object.get("uuid").getAsString();
		if (stringUUID == null)
			return null;
		UUID uuid = UUID.fromString(stringUUID);
		if (uuid == null)
			return null;
		Loadout loadout = new Loadout(uuid, character.get());
		JsonElement abilitiesElement = object.get("abilities");
		if (abilitiesElement != null && !abilitiesElement.isJsonNull() && abilitiesElement.isJsonArray()) {
			JsonArray array = abilitiesElement.getAsJsonArray();
			array.forEach(element -> {
				Optional<Ability> ability = characterManager.getAbility(element.getAsString());
				if (!ability.isPresent())
					return;
				loadout.setNextSlot(ability.get());
			});
		}
		return loadout;
	}

}
