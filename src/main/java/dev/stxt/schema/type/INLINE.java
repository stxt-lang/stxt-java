package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code INLINE} type: node with an inline value (after {@code :}), accepting children. */
public final class INLINE implements Type {
	/** Single instance of this type. */
	public static final INLINE INSTANCE = new INLINE();

	private INLINE() {
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
	}
}
