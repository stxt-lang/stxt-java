package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class TEXT implements Type {
	public static final TEXT INSTANCE = new TEXT();

	private TEXT() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }   
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (n.getChildren().size() > 0) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_CHILDREN_TEXT",
					"Not allowed children nodes in node " + n.getQualifiedName());
		}
	}
}
