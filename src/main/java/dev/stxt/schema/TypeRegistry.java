package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.exceptions.STXTException;
import dev.stxt.schema.type.BASE64;
import dev.stxt.schema.type.BINARY;
import dev.stxt.schema.type.BLOCK;
import dev.stxt.schema.type.BOOLEAN;
import dev.stxt.schema.type.DATE;
import dev.stxt.schema.type.EMAIL;
import dev.stxt.schema.type.ENUM;
import dev.stxt.schema.type.GROUP;
import dev.stxt.schema.type.HEXADECIMAL;
import dev.stxt.schema.type.INLINE;
import dev.stxt.schema.type.INTEGER;
import dev.stxt.schema.type.MARKDOWN;
import dev.stxt.schema.type.NATURAL;
import dev.stxt.schema.type.NUMBER;
import dev.stxt.schema.type.TEXT;
import dev.stxt.schema.type.TIME;
import dev.stxt.schema.type.TIMESTAMP;
import dev.stxt.schema.type.URL;
import dev.stxt.schema.type.UUID;

/** Static registry of the STXT value types, indexed by name. Adding a new type: a new {@link Type} class + {@code register(...)} here. */
public final class TypeRegistry {
	private static final Map<String, Type> REGISTRY = new HashMap<>();

	static {
		// Main types
		register(INLINE.INSTANCE);
		register(BLOCK.INSTANCE);

		// Subtypes
		register(TEXT.INSTANCE);
		register(BOOLEAN.INSTANCE);
		register(URL.INSTANCE);
		register(INTEGER.INSTANCE);
		register(NATURAL.INSTANCE);
		register(NUMBER.INSTANCE);
		register(DATE.INSTANCE);
		register(TIME.INSTANCE);
		register(TIMESTAMP.INSTANCE);
		register(UUID.INSTANCE);
		register(EMAIL.INSTANCE);
		register(HEXADECIMAL.INSTANCE);
		register(BINARY.INSTANCE);
		register(BASE64.INSTANCE);
		register(GROUP.INSTANCE);
		register(ENUM.INSTANCE);
		register(MARKDOWN.INSTANCE);
	}

	/**
	 * @param nodeType name of the type to look for.
	 * @return the {@link Type} registered under that name, or {@code null} if it does not exist.
	 */
	public static Type get(String nodeType) {
		return REGISTRY.get(nodeType);
	}

	// STXT-SCHEMA-SPEC 9 / STXT-TEMPLATE-SPEC 15: only INLINE and GROUP accept children
	/**
	 * @param nodeType name of the type.
	 * @return {@code true} if nodes of this type may have children (only INLINE and GROUP).
	 */
	public static boolean admitsChildren(String nodeType) {
		return "INLINE".equals(nodeType) || "GROUP".equals(nodeType);
	}

	private static void register(Type instance) {
		if (REGISTRY.containsKey(instance.getName()))
			throw new STXTException("DUPLICATED_TYPE", "Type already defined: " + instance.getName());
		
		REGISTRY.put(instance.getName(), instance);
	}

}
