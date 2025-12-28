package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

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
	private static final Map<String, TypeValidator> REGISTRY = new HashMap<>();

	static {
		// Tipos principales
		REGISTRY.put("VALUE_NODE", 	ValueNodeValidator.INSTANCE);
		REGISTRY.put("TEXT_NODE", 	TextNodeValidator.INSTANCE);
		
		// Subtipos
		REGISTRY.put("BOOLEAN", 	BooleanValidator.INSTANCE);
		REGISTRY.put("URL", 		UrlValidator.INSTANCE);
		REGISTRY.put("INTEGER", 	IntegerValidator.INSTANCE);
		REGISTRY.put("NATURAL", 	NaturalValidator.INSTANCE);
		REGISTRY.put("NUMBER", 		NumberValidator.INSTANCE);
		REGISTRY.put("DATE", 		DateValidator.INSTANCE);
		REGISTRY.put("TIMESTAMP",	TimestampValidator.INSTANCE);
		REGISTRY.put("EMAIL", 		EmailValidator.INSTANCE);
		REGISTRY.put("HEXADECIMAL",	HexadecimalValidator.INSTANCE);
		REGISTRY.put("BASE64", 		Base64Validator.INSTANCE);
		REGISTRY.put("EMPTY", 		EmptyValidator.INSTANCE);
		REGISTRY.put("TEXT", 		TextValidator.INSTANCE);
	}

	public static TypeValidator get(String nodeType) {
		return REGISTRY.get(nodeType);
	}

}
