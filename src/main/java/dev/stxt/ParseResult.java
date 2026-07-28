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

	public List<Node> getNodes() {
		return nodes;
	}

	public List<ParseException> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public void addError(ParseException error) {
		errors.add(error);
	}
}
