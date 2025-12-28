package dev.stxt.exceptions;

public class SchemaException extends STXTException {
    private static final long serialVersionUID = 1L;

	public SchemaException(String code, String message) {
        super(code, message);
    }
}
