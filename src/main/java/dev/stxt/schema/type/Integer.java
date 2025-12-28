package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class Integer extends RegexValue {
	private static final Pattern P_INTEGER = Pattern.compile("^(\\-|\\+)?\\d+$");
	public static final Integer INSTANCE = new Integer();

	private Integer() {
		super(P_INTEGER, "Invalid integer");
	}

	@Override
	public String getName() {
		return "INTEGER";
	}
}
