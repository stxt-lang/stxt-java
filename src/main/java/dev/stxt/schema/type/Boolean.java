package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class Boolean extends RegexValue {
	private static final Pattern P_BOOLEAN = Pattern.compile("^(true|false)$");
	public static final Boolean INSTANCE = new Boolean();

	private Boolean() {
		super(P_BOOLEAN, "Invalid boolean");
	}

	@Override
	public String getName() {
		return "BOOLEAN";
	}
}
