package dev.stxt;

import java.util.regex.Pattern;

import dev.stxt.exceptions.ParseException;

public class NamespaceValidator {
	/**
	 * Valida el namespace lógico.
	 *
	 * Reglas:
	 * - Solo minúsculas, dígitos y punto.
	 * - Puede empezar opcionalmente por '@'.
	 * - Debe ser una o varias etiquetas estilo dominio separadas por '.':
	 *   etiqueta := [a-z0-9]+
	 * ejemplos válidos: "xxx", "xxx.ddd", "zzz.ttt.ooo", "@xxx", "@xxx.ddd".
	 */
	private static final Pattern NAMESPACE_FORMAT = Pattern.compile("^@?[a-z0-9]+(\\.[a-z0-9]+)+$");
	public static void validateNamespaceFormat(String namespace, int lineNumber) {
		if (namespace == null || namespace.isEmpty())
			return;

		if (!NAMESPACE_FORMAT.matcher(namespace).matches())
			throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Namespace not valid: " + namespace);
	}
}
