package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code GROUP} type: container node with no value of its own, accepting children just like INLINE. */
public final class GROUP implements Type {
	/** Single instance of this type. */
	public static final GROUP INSTANCE = new GROUP();

	private GROUP() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		// NONE value form (STXT-SCHEMA-SPEC 9.2): neither an inline value nor a '>>' block
		if (n.isTextNode() || !n.getText().isEmpty()) {
			throw new ValidationException(n.getLine(), "VALUE_NOT_ALLOWED", "Node '" + n.getName() + "' has to be empty");
		}
	}
}
