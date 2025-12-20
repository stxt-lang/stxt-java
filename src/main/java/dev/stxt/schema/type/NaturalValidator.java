package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class NaturalValidator extends RegexValueValidator {
	private static final Pattern P_NATURAL = Pattern.compile("^\\d+$");
	public static final NaturalValidator INSTANCE = new NaturalValidator();

	private NaturalValidator() {
		super(P_NATURAL, "Invalid natural");
	}
}
