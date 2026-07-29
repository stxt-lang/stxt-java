package dev.stxt;

import java.util.regex.Pattern;

import dev.stxt.exceptions.ParseException;

/** Valida el formato de los namespaces {@code (a.b.c)} de STXT. */
public class NamespaceValidator {
	/**
	 * Formato del namespace lógico.
	 *
	 * Reglas:
	 * - Solo minúsculas, dígitos y punto.
	 * - Puede empezar opcionalmente por '@'.
	 * - Debe ser una o varias etiquetas estilo dominio separadas por '.':
	 *   etiqueta := [a-z0-9]+
	 * ejemplos válidos: "xxx", "xxx.ddd", "zzz.ttt.ooo", "@xxx", "@xxx.ddd".
	 */
	private static final Pattern NAMESPACE_FORMAT = Pattern.compile("^@?[a-z0-9]+(\\.[a-z0-9]+)+$");

	/**
	 * @param namespace namespace ya normalizado a validar; se ignora si es {@code null} o vacío.
	 * @param lineNumber número de línea, para el mensaje de error.
	 * @throws ParseException con código {@code INVALID_NAMESPACE} si no cumple el formato.
	 */
	public static void validateNamespaceFormat(String namespace, int lineNumber) {
		if (namespace == null || namespace.isEmpty())
			return;

		if (!NAMESPACE_FORMAT.matcher(namespace).matches())
			throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Namespace not valid: " + namespace);
	}
}
