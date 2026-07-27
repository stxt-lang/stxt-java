package dev.stxt.schema.type;

import dev.stxt.Node;

/**
 * STXT-SCHEMA-SPEC 9.5: valor efectivo para los tipos binarios INLINE/BLOCK
 * (HEXADECIMAL, BINARY, BASE64). En forma BLOCK, la validación se aplica sobre
 * la concatenación de las líneas del bloque, ignorando saltos de línea, líneas
 * vacías y espacios/tabuladores iniciales y finales de cada línea; el
 * whitespace interior de una línea NO se elimina en silencio.
 */
final class BinaryValue {
	private BinaryValue() {
	}

	static String get(Node n) {
		if (!n.isTextNode())
			return n.getValue();

		StringBuilder sb = new StringBuilder();
		for (String line : n.getTextLines())
			sb.append(line.trim());
		return sb.toString();
	}
}
