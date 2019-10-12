package me.limeglass.deadbycraft.placeholders;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Placeholders {

	private static Set<Placeholder<?>> placeholders = new HashSet<>();

	public static void registerPlaceholder(Placeholder<?> placeholder) {
		placeholders.add(placeholder);
	}

	/**
	 * Grab a placeholder by it's syntax.
	 * Example: %command% to be replaced by a String command.
	 * 
	 * @param syntax The syntax to grab e.g: %player%
	 * @return The placeholder if the syntax was found.
	 */
	public static Optional<Placeholder<?>> getPlaceholder(String syntax) {
		for (Placeholder<?> placeholder : placeholders) {
			for (String s : placeholder.getSyntaxes()) {
				if (s.equals(syntax)) {
					return Optional.of(placeholder);
				}
			}
		}
		return Optional.empty();
	}

	public static Set<Placeholder<?>> getPlaceholders() {
		return placeholders;
	}

}
