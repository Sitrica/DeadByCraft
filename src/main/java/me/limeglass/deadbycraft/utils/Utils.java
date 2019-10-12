package me.limeglass.deadbycraft.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import me.limeglass.deadbycraft.DeadByCraft;

public class Utils {

	private static JarFile getJar(DeadByCraft instance) {
		try {
			Method method = JavaPlugin.class.getDeclaredMethod("getFile");
			method.setAccessible(true);
			File file = (File) method.invoke(instance);
			return new JarFile(file);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isUUID(String uuid) {
		try {
			UUID.fromString(uuid);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static UUID getUniqueId(String uuid) {
		try {
			return UUID.fromString(uuid);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static boolean checkForMatch(List<String> matchers, String input) {
		Pattern pattern;
		Matcher matcher;
		int matches = 0;
		for (String match : matchers) {
			pattern = Pattern.compile(match);
			matcher = pattern.matcher(input);
			for (; matcher.find(); matches++);
		}
		return matches > 0;
	}
	
	public static void loadClasses(DeadByCraft instance, String basePackage, String... subPackages) {
		for (int i = 0; i < subPackages.length; i++) {
			subPackages[i] = subPackages[i].replace('.', '/') + "/";
		}
		JarFile jar = getJar(instance);
		if (jar == null)
			return;
		basePackage = basePackage.replace('.', '/') + "/";
		try {
			for (Enumeration<JarEntry> jarEntry = jar.entries(); jarEntry.hasMoreElements();) {
				String name = jarEntry.nextElement().getName();
				if (name.startsWith(basePackage) && name.endsWith(".class")) {
					for (String sub : subPackages) {
						if (name.startsWith(sub, basePackage.length())) {
							String clazz = name.replace("/", ".").substring(0, name.length() - 6);
							Class.forName(clazz, true, instance.getClass().getClassLoader());
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				jar.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> getClassesOf(DeadByCraft instance, String basePackage, Class<T> type) {
		JarFile jar = getJar(instance);
		if (jar == null)
			return null;
		basePackage = basePackage.replace('.', '/') + "/";
		List<Class<T>> classes = new ArrayList<>();
		try {
			for (Enumeration<JarEntry> jarEntry = jar.entries(); jarEntry.hasMoreElements();) {
				String name = jarEntry.nextElement().getName();
				if (name.startsWith(basePackage) && name.endsWith(".class")) {
					String className = name.replace("/", ".").substring(0, name.length() - 6);
					Class<?> clazz = null;
					try {
						clazz = Class.forName(className, true, instance.getClass().getClassLoader());
					} catch (ExceptionInInitializerError | ClassNotFoundException e) {
						DeadByCraft.consoleMessage("Class " + className + " was formatted incorrectly, report this to the developers or an addon developer for Kingdoms.");
						e.printStackTrace();
					}
					if (clazz == null)
						continue;
					if (type.isAssignableFrom(clazz))
						classes.add((Class<T>) clazz);
				}
			}
		} finally {
			try {
				jar.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return classes;
	}
	
	/**
	 * Tests whether a given class exists in the classpath.
	 * 
	 * @author Skript team.
	 * @param className The {@link Class#getCanonicalName() canonical name} of the class
	 * @return Whether the given class exists.
	 */
	public static boolean classExists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Tests whether a method exists in the given class.
	 * 
	 * @author Skript team.
	 * @param c The class
	 * @param methodName The name of the method
	 * @param parameterTypes The parameter types of the method
	 * @return Whether the given method exists.
	 */
	public static boolean methodExists(Class<?> c, String methodName, Class<?>... parameterTypes) {
		try {
			c.getDeclaredMethod(methodName, parameterTypes);
			return true;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
	}
	
	public static boolean methodExists(Class<?> c, String methodName) {
		try {
			c.getDeclaredMethod(methodName);
			return true;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
	}
	
	public static EntityType entityAttempt(String attempt, String fallback) {
		EntityType entity = null;
		try {
			entity = EntityType.valueOf(attempt.toUpperCase());
		} catch (Exception e) {
			try {
				entity = EntityType.valueOf(fallback);
			} catch (Exception e1) {}
		}
		if (entity == null)
			entity = EntityType.ARROW;
		return entity;
	}
	
	public static Material materialAttempt(String attempt, String fallback) {
		Material material = null;
		try {
			material = Material.valueOf(attempt.toUpperCase());
		} catch (Exception e) {
			try {
				material = Material.valueOf(fallback);
			} catch (Exception e1) {}
		}
		if (material == null)
			material = Material.CHEST;
		return material;
	}
	
	public static Sound soundAttempt(String attempt, String fallback) {
		Sound sound = null;
		try {
			sound = Sound.valueOf(attempt.toUpperCase());
		} catch (Exception e) {
			try {
				sound = Sound.valueOf(fallback);
			} catch (Exception e1) {}
		}
		if (sound == null)
			sound = Sound.ENTITY_PLAYER_LEVELUP;
		return sound;
	}
	
}
