package dev.stxt;

import java.util.ArrayList;
import java.util.List;

import dev.stxt.exceptions.ParseException;

/**
 * Resultado de un parseo en modo multi-error: acumula los nodos raíz obtenidos y todos los
 * errores encontrados (tanto de sintaxis como de validación), sin abortar en el primero.
 *
 * Ver {@link Parser#parseResult(String)}. Para el modo tradicional fail-fast, usar
 * {@link Parser#parse(String)}, que internamente usa este resultado y lanza el primer error.
 */
public class ParseResult {
	private final List<Node> nodes = new ArrayList<>();
	private final List<ParseException> errors = new ArrayList<>();

	/** @return nodos raíz acumulados hasta ahora. */
	public List<Node> getNodes() {
		return nodes;
	}

	/** @return errores de sintaxis o de validación acumulados hasta ahora, en orden de aparición. */
	public List<ParseException> getErrors() {
		return errors;
	}

	/** @return {@code true} si se ha acumulado al menos un error. */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	/** @param node nodo raíz ya cerrado a añadir al resultado. */
	public void addNode(Node node) {
		nodes.add(node);
	}

	/** @param error error encontrado durante el parseo, sin abortar el recorrido. */
	public void addError(ParseException error) {
		errors.add(error);
	}
}
