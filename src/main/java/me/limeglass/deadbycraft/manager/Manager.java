package me.limeglass.deadbycraft.manager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.database.Database;
import me.limeglass.deadbycraft.database.H2Database;
import me.limeglass.deadbycraft.database.MySQLDatabase;

public abstract class Manager implements Listener {

	private final Map<Class<?>, Database<?>> databases = new HashMap<>();
	private final boolean listener;

	protected Manager(boolean listener) {
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	protected <T> Database<T> getMySQLDatabase(String table, Class<T> type) {
		if (databases.containsKey(type))
			return (MySQLDatabase<T>) databases.get(type);
		ConfigurationSection section = DeadByCraft.getInstance().getConfig().getConfigurationSection("database");
		String address = section.getString("mysql.address", "localhost");
		String password = section.getString("mysql.password", "1234");
		String name = section.getString("mysql.name", "username");
		String user = section.getString("mysql.user", "root");
		Database<T> database = null;
		try {
			database = new MySQLDatabase<>(address, name, table, user, password, type);
			DeadByCraft.debugMessage("MySQL connection " + address + " was a success!");
			databases.put(type, (MySQLDatabase<?>) database);
			return database;
		} catch (SQLException exception) {
			DeadByCraft.consoleMessage("&cMySQL connection failed!");
			DeadByCraft.consoleMessage("Address: " + address + " with user: " + user);
			DeadByCraft.consoleMessage("Reason: " + exception.getMessage());
		} finally {
			if (database == null) {
				DeadByCraft.consoleMessage("Attempting to use SQLite instead...");
				database = getFileDatabase(table, type);
			}
		}
		return database;
	}

	@SuppressWarnings("unchecked")
	protected <T> Database<T> getFileDatabase(String table, Class<T> type) {
		if (databases.containsKey(type))
			return (H2Database<T>) databases.get(type);
		Database<T> database = null;
		try {
			database = new H2Database<>(table, type);
			DeadByCraft.debugMessage("Using H2 database for " + type.getSimpleName() + " data");
			databases.put(type, database);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return database;
	}

	public boolean hasListener() {
		return listener;
	}

}
