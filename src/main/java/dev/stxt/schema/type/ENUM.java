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
		if (n.isTextNode()) {
			throw new ValidationException(n.getLine(), "BLOCK_FORM_NOT_ALLOWED",
					"Not allowed text in node " + n.getQualifiedName());
		}
		
		String value = n.getText();
		if (!ndef.getValues().contains(value))
		    throw new ValidationException(n.getLine(), "INVALID_VALUE", "The value '" + value + "' not allowed. Only: " + ndef.getValues());
	}
}
