package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class ENUM implements Type {
	public static final ENUM INSTANCE = new ENUM();

	private ENUM() {
	}

	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (n.getTextLines().size() > 0) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_TEXT",
					"Not allowed text in node " + n.getQualifiedName());
		}
		
		if (!ndef.getValues().contains(n.getValue()))
		    throw new ValidationException(n.getLine(), "INVALID_VALUE", "The value '" + n.getValue() + "' not allowed. Only: " + ndef.getValues());
	}

	@Override
	public String getName() {
		return "ENUM";
	}
}
