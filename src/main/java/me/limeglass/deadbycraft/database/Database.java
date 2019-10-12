package me.limeglass.deadbycraft.database;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.limeglass.deadbycraft.database.serializer.ArenaInfoSerializer;
import me.limeglass.deadbycraft.database.serializer.BlockInfoSerializer;
import me.limeglass.deadbycraft.database.serializer.GamePlayerSerializer;
import me.limeglass.deadbycraft.database.serializer.ItemStackSerializer;
import me.limeglass.deadbycraft.database.serializer.LoadoutSerializer;
import me.limeglass.deadbycraft.database.serializer.LocationSerializer;
import me.limeglass.deadbycraft.objects.ArenaInfo;
import me.limeglass.deadbycraft.objects.BlockInfo;
import me.limeglass.deadbycraft.objects.GamePlayer;
import me.limeglass.deadbycraft.objects.Loadout;

public abstract class Database<T> {

	protected final Gson gson;

	public Database() {
		gson = new GsonBuilder()
				.registerTypeAdapter(GamePlayer.class, new GamePlayerSerializer())
				.registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
				.registerTypeAdapter(ArenaInfo.class, new ArenaInfoSerializer())
				.registerTypeAdapter(BlockInfo.class, new BlockInfoSerializer())
				.registerTypeAdapter(Location.class, new LocationSerializer())
				.registerTypeAdapter(Loadout.class, new LoadoutSerializer())
				.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
				.enableComplexMapKeySerialization()
				.serializeNulls()
				.create();
	}

	public abstract void put(String key, T value);

	public abstract T get(String key, T def);

	public abstract boolean has(String key);

	public abstract Set<String> getKeys();

	public T get(String key) {
		return get(key, null);
	}

	public void delete(String key) {
		put(key, null);
	}

	public abstract void clear();

	public String serialize(Object object, Type type) {
		return gson.toJson(object, type);
	}

	public Object deserialize(String json, Type type) {
		return gson.fromJson(json, type);
	}

}
