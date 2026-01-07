package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class INTEGER extends RegexValue {
	private static final Pattern P_INTEGER = Pattern.compile("^(\\-|\\+)?\\d+$");
	public static final INTEGER INSTANCE = new INTEGER();

	private INTEGER() {
		super(P_INTEGER, "Invalid integer");
	}

	@Override
	public String getName() {
		return "INTEGER";
	}
}
