package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** {@code BOOLEAN} type: checks that the value is {@code true} or {@code false}. */
public final class BOOLEAN extends RegexValue {
	private static final Pattern P_BOOLEAN = Pattern.compile("^(true|false)$");
	/** Single instance of this type. */
	public static final BOOLEAN INSTANCE = new BOOLEAN();

	private BOOLEAN() {
		super(P_BOOLEAN, "Invalid boolean");
	}
	
}
