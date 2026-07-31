package dev.stxt;

import java.util.regex.Pattern;

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
	private static final Pattern NAMESPACE_FORMAT = Pattern.compile("^@?[a-z0-9]+(\\.[a-z0-9]+)+$");

	/**
	 * @param namespace already normalized namespace to validate; ignored when {@code null} or empty.
	 * @param lineNumber line number, for the error message.
	 * @throws ParseException with code {@code INVALID_NAMESPACE} if it does not match the format.
	 */
	public static void validateNamespaceFormat(String namespace, int lineNumber) {
		if (namespace == null || namespace.isEmpty())
			return;

		if (!NAMESPACE_FORMAT.matcher(namespace).matches())
			throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Namespace not valid: " + namespace);
	}
}
