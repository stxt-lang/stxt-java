package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.STXTException;
import dev.stxt.schema.type.Base64Type;
import dev.stxt.schema.type.BooleanType;
import dev.stxt.schema.type.DateType;
import dev.stxt.schema.type.EmailType;
import dev.stxt.schema.type.EmptyType;
import dev.stxt.schema.type.HexadecimalTypeç;
import dev.stxt.schema.type.ValueNodeType;
import dev.stxt.schema.type.IntegerType;
import dev.stxt.schema.type.TextNodeType;
import dev.stxt.schema.type.NaturalType;
import dev.stxt.schema.type.NumberType;
import dev.stxt.schema.type.TextType;
import dev.stxt.schema.type.TimestampType;
import dev.stxt.schema.type.UrlType;

class TypeRegistry {
	private static final Map<String, Type> REGISTRY = new HashMap<>();

	static {
		// Tipos principales
		register(ValueNodeType.INSTANCE);
		register(TextNodeType.INSTANCE);
		
		// Subtipos
		register(TextType.INSTANCE);
		register(BooleanType.INSTANCE);
		register(UrlType.INSTANCE);
		register(IntegerType.INSTANCE);
		register(NaturalType.INSTANCE);
		register(NumberType.INSTANCE);
		register(DateType.INSTANCE);
		register(TimestampType.INSTANCE);
		register(EmailType.INSTANCE);
		register(HexadecimalTypeç.INSTANCE);
		register(Base64Type.INSTANCE);
		register(EmptyType.INSTANCE);
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
