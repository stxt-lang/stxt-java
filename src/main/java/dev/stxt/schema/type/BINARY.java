package dev.stxt.schema.type;

import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class BINARY implements Type {
	// STXT-SCHEMA-SPEC 9.5: cadena [01]+
	private static final Pattern P_BINARY = Pattern.compile("^[01]+$");

	public static final BINARY INSTANCE = new BINARY();

	private BINARY() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
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
