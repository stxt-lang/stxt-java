package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.schema.TypeValidator;
import dev.stxt.utils.StringUtils;

public final class HexadecimalValidator implements TypeValidator {
	public static final HexadecimalValidator INSTANCE = new HexadecimalValidator();

	private HexadecimalValidator() {
	}

	@Override
	public void validate(Node n) {
		// Elimina espacios, tabs y saltos de línea
		String value = StringUtils.cleanupString(n.getText());

		if (value.isEmpty()) {
			throw invalid(n, "Invalid hexadecimal (empty)");
		}

		// Permitir prefijo '#'
		if (value.startsWith("#")) {
			value = value.substring(1);
		}

		if (value.isEmpty()) {
			throw invalid(n, "Invalid hexadecimal (only '#')");
		}

		// Longitud par (hexadecimal por bytes)
		if ((value.length() & 1) != 0) {
			throw invalid(n, "Invalid hexadecimal length (must be even)");
		}

		// Validar caracteres hexadecimales
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.digit(c, 16) == -1) {
				throw invalid(n, "Invalid hexadecimal character '" + c + "'");
			}
		}
	}

	private static ParseException invalid(Node n, String msg) {
		return new ParseException(
				n.getLine(),
				"INVALID_VALUE",
				n.getName() + ": " + msg
		);
	}
}
