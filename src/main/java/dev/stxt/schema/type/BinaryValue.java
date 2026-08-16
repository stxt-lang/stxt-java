package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.TextNode;

/**
 * STXT-SCHEMA-SPEC 9.5: effective value for the INLINE/BLOCK binary types
 * (HEXADECIMAL, BINARY, BASE64). In BLOCK form, validation applies to the
 * concatenation of the lines of the block, ignoring line breaks, empty lines
 * and the leading and trailing spaces/tabs of each line; whitespace inside a
 * line is NOT silently removed.
 */
final class BinaryValue {
	private BinaryValue() {
	}

	static String get(Node n) {
		if (!(n instanceof TextNode text))
			return n.getText();

		StringBuilder sb = new StringBuilder();
		for (String line : text.getTextLines())
			sb.append(line.trim());
		return sb.toString();
	}
}
