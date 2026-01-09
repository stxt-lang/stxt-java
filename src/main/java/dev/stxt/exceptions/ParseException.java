package dev.stxt.exceptions;

public class ParseException extends STXTException {
	private static final long serialVersionUID = 1L;
	
	private final int line;
    
    public ParseException(int line, String code, String message) {
        super(code, "Error at line: " + line + ", " + message);
        this.line = line;
    }
    public int getLine() { return line; }
}
