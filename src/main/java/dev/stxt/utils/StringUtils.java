package dev.stxt.utils;

import java.text.Normalizer;
import java.util.Locale;

/** String normalization helpers used for names, namespaces and values. */
public class StringUtils {
	private StringUtils() {
	}

	// Used for name>> nodes
	/**
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
	 * @param input string to remove the spaces from.
	 * @return the string without any whitespace at all.
	 */
	public static String cleanSpaces(String input) {
		return input.replaceAll("\\s+", "");
	}
	
	// Used to normalize namespaces
	/**
	 * @param input string to lower-case.
	 * @return the lower-cased string; {@code null} is treated as the empty string.
	 */
	public static String lowerCase(String input) {
		if (input == null) return "";
		return input.toLowerCase(Locale.ROOT);
	}
	
	// Used for the name of the nodes
	/**
	 * @param s string to compact.
	 * @return the string with the outer spaces trimmed and the inner ones collapsed into a single one; {@code null} is treated as the empty string.
	 */
	public static String compactSpaces(String s) {
		if (s == null)
			return "";
		return s.trim().replaceAll("\\s+", " ");
	}

	// Used for the normalized name of the nodes (STXT-SPEC 4.3): NFC + lower case,
	// keeping diacritics and non-Latin alphabets (IDN model)
	/**
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
