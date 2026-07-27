package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class UUID extends RegexValue {
	private static final Pattern P_UUID = Pattern
			.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
	public static final UUID INSTANCE = new UUID();

	private UUID() {
		super(P_UUID, "Invalid UUID");
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
}
