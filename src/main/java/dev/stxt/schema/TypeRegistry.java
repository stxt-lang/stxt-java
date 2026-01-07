package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.exceptions.STXTException;
import dev.stxt.schema.type.BASE64;
import dev.stxt.schema.type.BLOCK;
import dev.stxt.schema.type.BOOLEAN;
import dev.stxt.schema.type.DATE;
import dev.stxt.schema.type.EMAIL;
import dev.stxt.schema.type.ENUM;
import dev.stxt.schema.type.GROUP;
import dev.stxt.schema.type.HEXADECIMAL;
import dev.stxt.schema.type.INLINE;
import dev.stxt.schema.type.INTEGER;
import dev.stxt.schema.type.NATURAL;
import dev.stxt.schema.type.NUMBER;
import dev.stxt.schema.type.TEXT;
import dev.stxt.schema.type.TIMESTAMP;
import dev.stxt.schema.type.URL;

class TypeRegistry {
	private static final Map<String, Type> REGISTRY = new HashMap<>();

	static {
		// Tipos principales
		register(INLINE.INSTANCE);
		register(BLOCK.INSTANCE);
		
		// Subtipos
		register(TEXT.INSTANCE);
		register(BOOLEAN.INSTANCE);
		register(URL.INSTANCE);
		register(INTEGER.INSTANCE);
		register(NATURAL.INSTANCE);
		register(NUMBER.INSTANCE);
		register(DATE.INSTANCE);
		register(TIMESTAMP.INSTANCE);
		register(EMAIL.INSTANCE);
		register(HEXADECIMAL.INSTANCE);
		register(BASE64.INSTANCE);
		register(GROUP.INSTANCE);
		register(ENUM.INSTANCE);
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
