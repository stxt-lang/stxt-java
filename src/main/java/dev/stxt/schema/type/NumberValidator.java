package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class NumberValidator extends RegexValueValidator {
	private static final Pattern P_NUMBER = Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+(e(\\-|\\+)?\\d+)?)?$");
	public static final NumberValidator INSTANCE = new NumberValidator();

	private NumberValidator() {
		super(P_NUMBER, "Invalid number");
	}
}
