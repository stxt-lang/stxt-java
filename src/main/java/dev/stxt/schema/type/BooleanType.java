package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class BooleanType extends RegexValueType {
	private static final Pattern P_BOOLEAN = Pattern.compile("^(true|false)$");
	public static final BooleanType INSTANCE = new BooleanType();

	private BooleanType() {
		super(P_BOOLEAN, "Invalid boolean");
	}

	@Override
	public String getName() {
		return "BOOLEAN";
	}
}
