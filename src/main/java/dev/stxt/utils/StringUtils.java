package dev.stxt.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** String normalization helpers used for names, namespaces and values. */
public class StringUtils {
	// STXT-SPEC 4.2 / 4.3: letters, decimal digits, combining marks (Mn, Mc) and the three
	// separators, with at least one letter or digit; checked on the NFC form
	private static final Pattern NODE_NAME = Pattern.compile("^[\\p{L}\\p{Nd}\\p{Mn}\\p{Mc}\\-_ ]+$");
	private static final Pattern NODE_NAME_LETTER_OR_DIGIT = Pattern.compile("[\\p{L}\\p{Nd}]");

	// STXT-SPEC 4: a blank is exactly U+0020 or U+0009. Every trim in the core works on these
	// two characters only; String.trim() (every code point <= U+0020), String.strip() and
	// Character.isWhitespace() are deliberately avoided because they are broader or narrower
	// than that, and STXT treats everything else (NBSP, U+3000, controls...) as content.
	private static final Pattern BLANK_RUN = Pattern.compile("[ \\t]+");

	private StringUtils() {
	}

	/**
	 * Tells whether a character is an STXT blank (STXT-SPEC 4): space or tab.
	 *
	 * @param c character.
	 * @return {@code true} for U+0020 and U+0009 only.
	 */
	public static boolean isBlank(char c) {
		return c == ' ' || c == '\t';
	}

	/**
	 * Removes the leading and trailing blanks (space and tab only, STXT-SPEC 4) of a string.
	 *
	 * @param s string to trim.
	 * @return the trimmed string; {@code null} is treated as the empty string.
	 */
	public static String trim(String s) {
		if (s == null)
			return "";
		int start = 0;
		int end = s.length();
		while (start < end && isBlank(s.charAt(start)))
			start++;
		while (end > start && isBlank(s.charAt(end - 1)))
			end--;
		return s.substring(start, end);
	}

	// Used for name>> nodes
	/**
	 * Removes the trailing blanks (space and tab only, STXT-SPEC 4, 10.2) of a string.
	 *
	 * @param s string to strip the trailing blanks from.
	 * @return the string without trailing blanks; {@code null} is treated as the empty string.
	 */
	public static String rightTrim(String s) {
		if (s == null)
			return "";
		int i = s.length() - 1;
		while (i >= 0 && isBlank(s.charAt(i))) {
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
	 * Trims a string and collapses its inner runs of blanks (space and tab only).
	 *
	 * @param s string to compact.
	 * @return the string with the outer blanks trimmed and the inner runs collapsed into a single space; {@code null} is treated as the empty string.
	 */
	public static String compactSpaces(String s) {
		return BLANK_RUN.matcher(trim(s)).replaceAll(" ");
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
		return NODE_NAME.matcher(nfc).matches() && NODE_NAME_LETTER_OR_DIGIT.matcher(nfc).find();
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
	    String s = trim(input);
	    if (s.isEmpty()) return "";

	    s = Normalizer.normalize(s, Normalizer.Form.NFC);
	    s = s.toLowerCase(Locale.ROOT);

	    // every run of separators ('-', '_', blanks) => a single '-'
	    s = s.replaceAll("[-_ \\t]+", "-");

	    // trim the '-'
	    s = s.replaceAll("^-+|-+$", "");
	    return s;
	}
}
