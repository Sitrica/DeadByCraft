package me.limeglass.deadbycraft.placeholders;

public abstract class SimplePlaceholder extends Placeholder<String> {
	
	public SimplePlaceholder(String... syntax) {
		super(syntax);
	}

	@Override
	public final String replace(String object) {
		return get();
	}
	
	public abstract String get();
	
}
