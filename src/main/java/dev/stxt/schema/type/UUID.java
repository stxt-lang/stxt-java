package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** {@code UUID} type: checks the standard UUID format ({@code 8-4-4-4-12} hexadecimal). */
public final class UUID extends RegexValue {
	private static final Pattern P_UUID = Pattern
			.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
	/** Single instance of this type. */
	public static final UUID INSTANCE = new UUID();

	private UUID() {
		super(P_UUID, "Invalid UUID");
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
}
