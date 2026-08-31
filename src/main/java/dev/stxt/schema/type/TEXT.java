package dev.stxt.schema.type;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code TEXT} type: free text, in inline or block form; only children are forbidden. */
public final class TEXT implements Type {
	/** Single instance of this type. */
	public static final TEXT INSTANCE = new TEXT();

	private TEXT() {
	}

    @Override
    public String getName() {
        return "TEXT";
    }   
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (n instanceof InlineNode inline && !inline.getChildren().isEmpty()) {
			throw new ValidationException(n.getLine(), "CHILDREN_NOT_ALLOWED",
					"Not allowed children nodes in node " + n.getQualifiedName());
		}
	}
}
