package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.exceptions.STXTException;
import dev.stxt.schema.type.Base64;
import dev.stxt.schema.type.Boolean;
import dev.stxt.schema.type.Date;
import dev.stxt.schema.type.Email;
import dev.stxt.schema.type.Empty;
import dev.stxt.schema.type.Hexadecimal;
import dev.stxt.schema.type.ValueNode;
import dev.stxt.schema.type.Integer;
import dev.stxt.schema.type.TextNode;
import dev.stxt.schema.type.Natural;
import dev.stxt.schema.type.Number;
import dev.stxt.schema.type.Text;
import dev.stxt.schema.type.Timestamp;
import dev.stxt.schema.type.Url;

class TypeRegistry {
	private static final Map<String, Type> REGISTRY = new HashMap<>();

	static {
		// Tipos principales
		register(ValueNode.INSTANCE);
		register(TextNode.INSTANCE);
		
		// Subtipos
		register(Text.INSTANCE);
		register(Boolean.INSTANCE);
		register(Url.INSTANCE);
		register(Integer.INSTANCE);
		register(Natural.INSTANCE);
		register(Number.INSTANCE);
		register(Date.INSTANCE);
		register(Timestamp.INSTANCE);
		register(Email.INSTANCE);
		register(Hexadecimal.INSTANCE);
		register(Base64.INSTANCE);
		register(Empty.INSTANCE);
	}

	public static Type get(String nodeType) {
		return REGISTRY.get(nodeType);
	}

	private static void register(Type instance) {
		if (REGISTRY.containsKey(instance.getName()))
			throw new STXTException("DUPLICATED_TYPE", "Type already defined: " + instance.getName());
		
		REGISTRY.put(instance.getName(), instance);
	}

}
