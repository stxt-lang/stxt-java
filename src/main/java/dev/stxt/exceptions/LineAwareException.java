package dev.stxt.exceptions;

public abstract class LineAwareException extends STXTException {
	private static final long serialVersionUID = 1L;
	
	private final int line;
    
    protected LineAwareException(int line, String code, String message) {
        super(code, message);
        this.line = line;
    }
    
    public int getLine() { return line; }
}
