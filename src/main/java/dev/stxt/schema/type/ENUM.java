package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code ENUM} type: checks that the value is one of those declared in {@link NodeDefinition#getValues()}. */
public final class ENUM implements Type {
	/** Single instance of this type. */
	public static final ENUM INSTANCE = new ENUM();

	private ENUM() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
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
}
