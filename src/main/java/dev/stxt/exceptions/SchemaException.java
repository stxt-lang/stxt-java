package dev.stxt.exceptions;

/** Error de un documento {@code @stxt.schema}/{@code @stxt.template} mal formado. */
public class SchemaException extends STXTException {
    private static final long serialVersionUID = 1L;

	/**
	 * @param code código de error en MAYÚSCULAS.
	 * @param message mensaje descriptivo.
	 */
	public SchemaException(String code, String message) {
        super(code, message);
    }
}
