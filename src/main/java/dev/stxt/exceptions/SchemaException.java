package dev.stxt.exceptions;

/** Error caused by a malformed {@code @stxt.schema}/{@code @stxt.template} document. */
public class SchemaException extends STXTException {
    private static final long serialVersionUID = 1L;

	/**
	 * @param code error code in UPPERCASE.
	 * @param message descriptive message.
	 */
	public SchemaException(String code, String message) {
        super(code, message);
    }
}
