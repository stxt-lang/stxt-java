package dev.stxt.schema.type;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/**
 * STXT-SCHEMA-SPEC 9.7: for validation purposes it is equivalent to TEXT (any
 * content is valid Markdown); only children are forbidden.
 */
public final class MARKDOWN implements Type {
	/** Single instance of this type. */
	public static final MARKDOWN INSTANCE = new MARKDOWN();

	private MARKDOWN() {
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
