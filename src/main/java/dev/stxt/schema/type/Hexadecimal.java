package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;
import dev.stxt.utils.StringUtils;

public final class Hexadecimal implements Type {
	public static final Hexadecimal INSTANCE = new Hexadecimal();

	private Hexadecimal() {
	}

	@Override
    public void validate(NodeDefinition ndef, Node n) {
		// Elimina espacios, tabs y saltos de línea
		String value = StringUtils.cleanSpaces(n.getText());

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

	private static ValidationException invalid(Node n, String msg) {
		return new ValidationException(
				n.getLine(),
				"INVALID_VALUE",
				n.getName() + ": " + msg
		);
	}

	@Override
	public String getName() {
		return "HEXADECIMAL";
	}
}
