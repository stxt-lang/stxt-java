package dev.stxt;

public class STXTException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private final String code;
    
    public STXTException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public STXTException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() { return code; }
}
