package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class BooleanValidator extends RegexValueValidator {
	private static final Pattern P_BOOLEAN = Pattern.compile("^(true|false)$");
	public static final BooleanValidator INSTANCE = new BooleanValidator();

	private BooleanValidator() {
		super(P_BOOLEAN, "Invalid boolean");
	}

	@Override
	public String getName() {
		return "BOOLEAN";
	}
}
