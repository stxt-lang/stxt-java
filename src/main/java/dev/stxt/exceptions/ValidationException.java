package dev.stxt.exceptions;

/** Semantic validation error (schema, type or cardinality), detected when a node is closed. */
public class ValidationException extends ParseException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param line line number where the error was detected.
	 * @param code error code in UPPERCASE.
	 * @param message descriptive message.
	 */
	public ValidationException(int line, String code, String message) {
        super(line, code, message);
    }
}
