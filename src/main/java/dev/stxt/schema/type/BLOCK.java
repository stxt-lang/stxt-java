package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code BLOCK} type: text block node ({@code >>}), with no further restriction on the content. */
public final class BLOCK implements Type {
	/** Single instance of this type. */
	public static final BLOCK INSTANCE = new BLOCK();

	private BLOCK() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		// BLOCK value form (STXT-SCHEMA-SPEC 9.2): only the '>>' block, not the inline form
		if (!n.isTextNode()) {
			throw new ValidationException(n.getLine(), "BLOCK_FORM_REQUIRED",
					"Node " + n.getQualifiedName() + " requires block form '>>'");
		}
	}

}
