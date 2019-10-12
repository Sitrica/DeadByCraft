package me.limeglass.deadbycraft.objects;

import java.util.Collection;
import java.util.function.Function;

public class StringList {

	private final StringBuilder builder = new StringBuilder();
	private final String[] strings;

	public StringList(String... strings) {
		this.strings = strings;
	}

	public StringList(Collection<String> collection) {
		this.strings = collection.toArray(new String[collection.size()]);
	}

	public <T> StringList(Collection<T> collection, Function<T, String> mapper) {
		this.strings = collection.parallelStream().map(mapper).toArray(String[]::new);
	}

	public String merge() {
		for (String string : strings)
			builder.append(string + " ");
		return builder.toString();
	}

	@Override
	public String toString() {
		int i = 1;
		for (String string : strings) {
			if (i >= strings.length)
				builder.append(string);
			else if (i == strings.length - 1)
				builder.append(string + ", and ");
			else
				builder.append(string + ", ");
			i++;
		}
		return builder.toString();
	}

}
