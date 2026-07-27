package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class GROUP implements Type {
	public static final GROUP INSTANCE = new GROUP();

	private GROUP() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		// Forma del valor NONE (STXT-SCHEMA-SPEC 9.2): ni valor inline ni bloque '>>'
		if (!n.getValue().isEmpty() || n.isTextNode()) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' has to be empty");
		}
	}
}
