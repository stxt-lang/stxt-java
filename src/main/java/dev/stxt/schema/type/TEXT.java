package dev.stxt.schema.type;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code TEXT} type: free text block node, with no children allowed. */
public final class TEXT implements Type {
	/** Single instance of this type. */
	public static final TEXT INSTANCE = new TEXT();

	private TEXT() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }   
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (n instanceof InlineNode inline && !inline.getChildren().isEmpty()) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_CHILDREN_TEXT",
					"Not allowed children nodes in node " + n.getQualifiedName());
		}
	}
}
