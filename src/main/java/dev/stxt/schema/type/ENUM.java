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
        return "ENUM";
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (n.isTextNode()) {
			throw new ValidationException(n.getLine(), "BLOCK_FORM_NOT_ALLOWED",
					"Not allowed text in node " + n.getQualifiedName());
		}
		
		String value = n.getText();
		if (!ndef.getValues().contains(value))
			// The message deliberately does not list the allowed values: every invalid node
			// would carry a copy of the whole set, and a large ENUM times a document with many
			// invalid nodes multiplies memory (1 836 values × 50 000 nodes gave 600 MB of
			// messages). The set stays available through NodeDefinition.getValues().
		    throw new ValidationException(n.getLine(), "INVALID_VALUE", "The value '" + value + "' is not one of the allowed values of " + ndef.getName());
	}
}
