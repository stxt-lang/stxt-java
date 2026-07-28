package dev.stxt.processors;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;

public interface Validator {
	/**
	 * Valida un nodo y devuelve todos los errores encontrados (sin lanzar excepción), permitiendo
	 * al llamador acumular errores de varios nodos en vez de abortar en el primero. Una lista vacía
	 * indica que el nodo es válido.
	 */
	List<ValidationException> validate(Node n);
}
