package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class HexadecimalValidator extends RegexValueValidator {
	private static final Pattern P_HEXADECIMAL = Pattern.compile("^\\#?([A-Fa-f0-9]|\\s)+$");
	public static final HexadecimalValidator INSTANCE = new HexadecimalValidator();

	private HexadecimalValidator() {
		super(P_HEXADECIMAL, "Invalid hexadecimal");
	}
}
