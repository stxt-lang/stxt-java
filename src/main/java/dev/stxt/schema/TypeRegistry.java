package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.STXTException;
import dev.stxt.schema.type.Base64Validator;
import dev.stxt.schema.type.BooleanValidator;
import dev.stxt.schema.type.DateValidator;
import dev.stxt.schema.type.EmailValidator;
import dev.stxt.schema.type.EmptyValidator;
import dev.stxt.schema.type.HexadecimalValidator;
import dev.stxt.schema.type.ValueNodeValidator;
import dev.stxt.schema.type.IntegerValidator;
import dev.stxt.schema.type.TextNodeValidator;
import dev.stxt.schema.type.NaturalValidator;
import dev.stxt.schema.type.NumberValidator;
import dev.stxt.schema.type.TextValidator;
import dev.stxt.schema.type.TimestampValidator;
import dev.stxt.schema.type.UrlValidator;

class TypeRegistry {
	private static final Map<String, Type> REGISTRY = new HashMap<>();

	static {
		// Tipos principales
		register(ValueNodeValidator.INSTANCE);
		register(TextNodeValidator.INSTANCE);
		
		// Subtipos
		register(TextValidator.INSTANCE);
		register(BooleanValidator.INSTANCE);
		register(UrlValidator.INSTANCE);
		register(IntegerValidator.INSTANCE);
		register(NaturalValidator.INSTANCE);
		register(NumberValidator.INSTANCE);
		register(DateValidator.INSTANCE);
		register(TimestampValidator.INSTANCE);
		register(EmailValidator.INSTANCE);
		register(HexadecimalValidator.INSTANCE);
		register(Base64Validator.INSTANCE);
		register(EmptyValidator.INSTANCE);
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
