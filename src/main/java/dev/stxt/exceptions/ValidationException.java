package dev.stxt.exceptions;

/** Error de validación semántica (schema, tipo o cardinalidad), detectado al cerrar un nodo. */
public class ValidationException extends ParseException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param line número de línea donde se detectó el error.
	 * @param code código de error en MAYÚSCULAS.
	 * @param message mensaje descriptivo.
	 */
	public ValidationException(int line, String code, String message) {
        super(line, code, message);
    }
}
