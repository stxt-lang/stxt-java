package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class IntegerType extends RegexValueType {
	private static final Pattern P_INTEGER = Pattern.compile("^(\\-|\\+)?\\d+$");
	public static final IntegerType INSTANCE = new IntegerType();

	private IntegerType() {
		super(P_INTEGER, "Invalid integer");
	}

	@Override
	public String getName() {
		return "INTEGER";
	}
}
