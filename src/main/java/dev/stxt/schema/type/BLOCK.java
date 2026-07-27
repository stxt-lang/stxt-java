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
		// Forma del valor BLOCK (STXT-SCHEMA-SPEC 9.2): sólo bloque '>>', no forma inline
		if (!n.isTextNode()) {
			throw new ValidationException(n.getLine(), "BLOCK_FORM_REQUIRED",
					"Node " + n.getQualifiedName() + " requires block form '>>'");
		}
	}

}
