package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class BLOCK implements Type {
	public static final BLOCK INSTANCE = new BLOCK();

	private BLOCK() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (!n.getValue().isEmpty()) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_VALUE",
					"Not allowed inline text in node " + n.getQualifiedName());
		}
	}

}
