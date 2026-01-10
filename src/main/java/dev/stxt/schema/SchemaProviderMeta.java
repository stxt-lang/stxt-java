package dev.stxt.schema;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.SchemaException;

public final class SchemaProviderMeta implements SchemaProvider {
	private static final String META_TEXT = """
			Schema (@stxt.schema): @stxt.schema
			    Node: Schema
			        Children:
			            Child: Description
			                Max: 1
			            Child: Node
			                Min: 1
			    Node: Node
			        Children:
			            Child: Type
			                Max: 1
			            Child: Children
			                Max: 1
			            Child: Description
			                Max: 1
			            Child: Values
			                Max: 1
			    Node: Children
			       	Type: GROUP
			        Children:
			            Child: Child
			                Min: 1
			    Node: Description
			        Type: TEXT
			    Node: Child
			        Children:
			            Child: Min
			                Max: 1
			            Child: Max
			                Max: 1
			    Node: Min
			        Type: NATURAL
			    Node: Max
			        Type: NATURAL
			    Node: Type
			    Node: Values
			    	Children:
			    		Child: Value
			    			Min: 1
			    Node: Value			
			""";
	
	private final Schema meta;

	public SchemaProviderMeta() {
		Schema metaSchema = null;
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(META_TEXT);
		if (nodes.size() != 1)
		    throw new SchemaException("META_SCHEMA_INVALID", "Meta schema must produce exactly 1 document, got " + nodes.size());
		metaSchema = SchemaParser.transformNodeToSchema(nodes.get(0));
		meta = metaSchema;
	}

	public Schema getSchema(String namespace) {
	    if (!Schema.SCHEMA_NAMESPACE.equals(namespace))
	        throw new ResourceNotFoundException(Schema.SCHEMA_NAMESPACE, namespace);

	    if (meta == null)
	        throw new SchemaException("META_SCHEMA_NOT_AVAILABLE", "Meta schema not available");

	    return meta;
	}
}
