package dev.stxt.processors;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;

/** Hook de proceso invocado por el {@link dev.stxt.Parser} al cerrar cada nodo, para validar en streaming. */
public interface Validator {
	/**
	 * Valida un nodo y devuelve todos los errores encontrados (sin lanzar excepción), permitiendo
	 * al llamador acumular errores de varios nodos en vez de abortar en el primero. Una lista vacía
	 * indica que el nodo es válido.
	 *
	 * @param n nodo ya cerrado a validar.
	 * @return los errores de validación encontrados, o una lista vacía si el nodo es válido.
	 */
	List<ValidationException> validate(Node n);
}
