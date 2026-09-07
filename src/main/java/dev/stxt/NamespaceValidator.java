package dev.stxt;

import dev.stxt.exceptions.ParseException;

/** Validates the format of STXT {@code (a.b.c)} namespaces. */
public class NamespaceValidator {
	/**
	 * Format of the logical namespace.
	 *
	 * Rules:
	 * - Lower-case letters, digits and dot only.
	 * - It may optionally start with '@'.
	 * - It must be one or more domain-style labels separated by '.':
	 *   label := [a-z0-9]+
	 * valid examples: "xxx", "xxx.ddd", "zzz.ttt.ooo", "@xxx", "@xxx.ddd".
	 */
	private NamespaceValidator() {
	}

	/**
	 * Tells whether a namespace matches the format, without throwing.
	 *
	 * Format of the logical namespace (STXT-SPEC 7): lower-case ASCII letters, digits and dot
	 * only; an optional leading {@code @} (reserved namespaces); two or more domain-style labels
	 * {@code [a-z0-9]+} separated by {@code .}. Checked by a hand-written scan rather than the
	 * regex {@code ^@?[a-z0-9]+(\.[a-z0-9]+)+$} used until 2026-09-06: java.util.regex implements
	 * a repeated group by recursion and overflowed the stack with ~2 000 labels — a 4 000-character
	 * line, well within the line limit — and a {@link StackOverflowError} is not an exception the
	 * parser can turn into an error. The scan is linear and identical in every port.
	 *
	 * @param namespace already normalized namespace to check.
	 * @return {@code true} if it matches the format; {@code false} when it is null, empty or malformed.
	 */
	public static boolean isValid(String namespace) {
		if (namespace == null || namespace.isEmpty())
			return false;
		int n = namespace.length();
		int i = namespace.charAt(0) == '@' ? 1 : 0;
		int labels = 0;
		while (true) {
			int start = i;
			while (i < n && isLabelChar(namespace.charAt(i)))
				i++;
			if (i == start)
				return false;		// empty label: "", "@", "a.", ".a", "a..b"
			labels++;
			if (i == n)
				return labels >= 2;
			if (namespace.charAt(i) != '.')
				return false;
			i++;
		}
	}

	// [a-z0-9], ASCII only
	private static boolean isLabelChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
	}

	/**
	 * Validates the format of a namespace.
	 *
	 * @param namespace already normalized namespace to validate; ignored when {@code null} or empty.
	 * @param lineNumber line number, for the error message.
	 * @throws ParseException with code {@code INVALID_NAMESPACE} if it does not match the format.
	 */
	public static void validateNamespaceFormat(String namespace, int lineNumber) {
		if (namespace == null || namespace.isEmpty())
			return;

		if (!isValid(namespace))
			throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Namespace not valid: " + namespace);
	}
}
