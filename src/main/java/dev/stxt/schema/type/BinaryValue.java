package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.TextNode;

/**
 * STXT-SCHEMA-SPEC 9.5: effective value for the INLINE/BLOCK binary types
 * (HEXADECIMAL, BINARY, BASE64). Every blank (U+0020 space, U+0009 tab) is
 * removed wherever it is, in both forms; in BLOCK form the lines are
 * concatenated first, which also drops line breaks and empty lines. So
 * {@code DE AD BE EF}, {@code 1010 1010} and Base64 wrapped at 76 columns
 * validate. No other character is removed: {@code DE:AD} or {@code DE-AD}
 * stay invalid.
 */
final class BinaryValue {
	private BinaryValue() {
	}

	static String get(Node n) {
		String raw;
		if (n instanceof TextNode text)
			raw = String.join("", text.getTextLines());
		else
			raw = n.getText();
		return removeBlanks(raw);
	}

	/** Removes every U+0020 and U+0009 from the string; nothing else. */
	static String removeBlanks(String s) {
		if (s == null)
			return "";
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != ' ' && c != '\t')
				sb.append(c);
		}
		return sb.toString();
	}
}
