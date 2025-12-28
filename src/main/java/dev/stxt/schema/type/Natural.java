package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class Natural extends RegexValue {
	private static final Pattern P_NATURAL = Pattern.compile("^\\d+$");
	public static final Natural INSTANCE = new Natural();

	private Natural() {
		super(P_NATURAL, "Invalid natural");
	}

	@Override
	public String getName() {
		return "NATURAL";
	}
}
