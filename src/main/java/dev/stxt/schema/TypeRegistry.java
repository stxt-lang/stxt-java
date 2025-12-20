package dev.stxt.schema;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.schema.type.Base64Validator;
import dev.stxt.schema.type.BooleanValidator;
import dev.stxt.schema.type.DateValidator;
import dev.stxt.schema.type.EmailValidator;
import dev.stxt.schema.type.EmptyValidator;
import dev.stxt.schema.type.HexadecimalValidator;
import dev.stxt.schema.type.InlineTextValidator;
import dev.stxt.schema.type.IntegerValidator;
import dev.stxt.schema.type.MultilineTextValidator;
import dev.stxt.schema.type.NaturalValidator;
import dev.stxt.schema.type.NumberValidator;
import dev.stxt.schema.type.TextValidator;
import dev.stxt.schema.type.TimestampValidator;
import dev.stxt.schema.type.UrlValidator;

class TypeRegistry {
	private static final String TEXT = "TEXT";
	private static final String INLINE_TEXT = "INLINE TEXT";
	private static final String MULTILINE_TEXT = "MULTILINE TEXT";
	private static final String NUMBER = "NUMBER";
	private static final String BOOLEAN = "BOOLEAN";
	private static final String DATE = "DATE";
	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String EMAIL = "EMAIL";
	private static final String URL = "URL";
	private static final String HEXADECIMAL = "HEXADECIMAL";
	private static final String BASE64 = "BASE64";
	private static final String EMPTY = "EMPTY";
	private static final String INTEGER = "INTEGER";
	private static final String NATURAL = "NATURAL";

	private static final Map<String, TypeValidator> REGISTRY = new HashMap<>();

	static {
		REGISTRY.put(BOOLEAN, BooleanValidator.INSTANCE);
		REGISTRY.put(URL, UrlValidator.INSTANCE);
		REGISTRY.put(INTEGER, IntegerValidator.INSTANCE);
		REGISTRY.put(NATURAL, NaturalValidator.INSTANCE);
		REGISTRY.put(NUMBER, NumberValidator.INSTANCE);
		REGISTRY.put(DATE, DateValidator.INSTANCE);
		REGISTRY.put(TIMESTAMP, TimestampValidator.INSTANCE);
		REGISTRY.put(EMAIL, EmailValidator.INSTANCE);
		REGISTRY.put(HEXADECIMAL, HexadecimalValidator.INSTANCE);
		REGISTRY.put(BASE64, Base64Validator.INSTANCE);
		REGISTRY.put(EMPTY, EmptyValidator.INSTANCE);
		REGISTRY.put(TEXT, TextValidator.INSTANCE);
		REGISTRY.put(INLINE_TEXT, InlineTextValidator.INSTANCE);
		REGISTRY.put(MULTILINE_TEXT, MultilineTextValidator.INSTANCE);
	}

	public static TypeValidator get(String nodeType) {
		return REGISTRY.get(nodeType);
	}

}
