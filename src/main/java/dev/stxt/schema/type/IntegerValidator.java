package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class IntegerValidator extends RegexValueValidator {
	private static final Pattern P_INTEGER = Pattern.compile("^(\\-|\\+)?\\d+$");
	public static final IntegerValidator INSTANCE = new IntegerValidator();

	private IntegerValidator() {
		super(P_INTEGER, "Invalid integer");
	}
}
