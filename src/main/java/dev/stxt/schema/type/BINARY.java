package dev.stxt.schema.type;

import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code BINARY} type: checks that the content is a string of zeros and ones ({@code [01]+}). */
public final class BINARY implements Type {
	// STXT-SCHEMA-SPEC 9.5: [01]+ string
	private static final Pattern P_BINARY = Pattern.compile("^[01]+$");

	/** Single instance of this type. */
	public static final BINARY INSTANCE = new BINARY();

	private BINARY() {
	}

    @Override
    public String getName() {
        return "BINARY";
    }

	@Override
    public void validate(NodeDefinition ndef, Node n) {
		String value = BinaryValue.get(n);
		if (!P_BINARY.matcher(value).matches()) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE",
					n.getName() + ": Invalid binary (" + value + ")");
		}
	}
}
