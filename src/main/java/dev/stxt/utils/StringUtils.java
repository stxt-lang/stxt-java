package dev.stxt.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** String normalization helpers used for names, namespaces and values. */
public class StringUtils {
	private static final Pattern NODE_NAME = Pattern.compile("^[\\p{L}\\p{Nd}\\-_ ]+$");

	private StringUtils() {
	}

	// Used for name>> nodes
	/**
	 * Removes the trailing whitespace of a string.
	 *
	 * @param s string to strip the trailing spaces from.
	 * @return the string without trailing whitespace; {@code null} is treated as the empty string.
	 */
	public static String rightTrim(String s) {
		if (s == null)
			return "";
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
	}

	// Used for Base64 and Hex typed nodes
	/**
	 * Removes every whitespace character of a string.
	 *
	 * @param input string to remove the spaces from.
	 * @return the string without any whitespace at all.
	 */
	public static String cleanSpaces(String input) {
		return input.replaceAll("\\s+", "");
	}

	// Used to normalize namespaces
	/**
	 * Lower-cases a string.
	 *
	 * @param input string to lower-case.
	 * @return the lower-cased string; {@code null} is treated as the empty string.
	 */
	public static String lowerCase(String input) {
		if (input == null) return "";
		return input.toLowerCase(Locale.ROOT);
	}
	
	// Used for the name of the nodes
	/**
	 * Trims a string and collapses its inner whitespace.
	 *
	 * @param s string to compact.
	 * @return the string with the outer spaces trimmed and the inner ones collapsed into a single one; {@code null} is treated as the empty string.
	 */
	public static String compactSpaces(String s) {
		if (s == null)
			return "";
		return s.trim().replaceAll("\\s+", " ");
	}

	/**
	 * Tells whether a value is a valid STXT node name.
	 *
	 * The character check happens after NFC normalization so a decomposed spelling such
	 * as {@code e + combining acute} is accepted as the equivalent {@code é}.
	 *
	 * @param input name to validate.
	 * @return {@code true} when the name uses permitted characters and has a non-empty canonical form.
	 */
	public static boolean isValidNodeName(String input) {
		String nfc = Normalizer.normalize(compactSpaces(input), Normalizer.Form.NFC);
		return NODE_NAME.matcher(nfc).matches() && !normalize(nfc).isEmpty();
	}

	// Used for the normalized name of the nodes (STXT-SPEC 4.3): NFC + lower case,
	// keeping diacritics and non-Latin alphabets (IDN model)
	/**
	 * Builds the canonical name of a node, as defined by STXT-SPEC 4.3.
	 *
	 * @param input string to normalize.
	 * @return the canonical name of a node: NFC + lower case, with separators collapsed into '-'; {@code null} is treated as the empty string.
	 */
	public static String normalize(String input) {
	    if (input == null) return "";
	    String s = input.trim();
	    if (s.isEmpty()) return "";

	    s = Normalizer.normalize(s, Normalizer.Form.NFC);
	    s = s.toLowerCase(Locale.ROOT);

	    // every run of separators ('-', '_', spaces) => a single '-'
	    s = s.replaceAll("[-_\\s]+", "-");

	    // trim the '-'
	    s = s.replaceAll("^-+|-+$", "");
	    return s;
	}
}
