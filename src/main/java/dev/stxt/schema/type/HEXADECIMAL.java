package dev.stxt.schema.type;

import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code HEXADECIMAL} type: checks a hexadecimal string ({@code [0-9A-Fa-f]+}), with no prefix and no mandatory even length. */
public final class HEXADECIMAL implements Type {
	// STXT-SCHEMA-SPEC 9.5: [0-9A-Fa-f]+ string, with no '#' prefix and no even-length requirement
	private static final Pattern P_HEX = Pattern.compile("^[0-9A-Fa-f]+$");

	/** Single instance of this type. */
	public static final HEXADECIMAL INSTANCE = new HEXADECIMAL();

	private HEXADECIMAL() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		String value = BinaryValue.get(n);
		if (!P_HEX.matcher(value).matches()) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE",
					n.getName() + ": Invalid hexadecimal (" + value + ")");
		}
	}
}
