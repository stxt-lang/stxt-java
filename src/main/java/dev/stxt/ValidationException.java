package dev.stxt;

public class ValidationException extends ParseException {
	private static final long serialVersionUID = 1L;
	public ValidationException(int line, String code, String message) {
        super(line, code, message);
    }
}
