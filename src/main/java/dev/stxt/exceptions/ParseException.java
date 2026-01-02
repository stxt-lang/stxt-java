package dev.stxt.exceptions;

public class ParseException extends LineAwareException {
	private static final long serialVersionUID = 1L;
	
    public ParseException(int line, String code, String message) {
        super(line, code, message);
    }
}
