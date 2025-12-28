package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class NaturalType extends RegexValueType {
	private static final Pattern P_NATURAL = Pattern.compile("^\\d+$");
	public static final NaturalType INSTANCE = new NaturalType();

	private NaturalType() {
		super(P_NATURAL, "Invalid natural");
	}

	@Override
	public String getName() {
		return "NATURAL";
	}
}
