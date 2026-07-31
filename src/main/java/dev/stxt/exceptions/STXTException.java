package dev.stxt.exceptions;

/** Root of every STXT exception. Every exception carries an UPPERCASE code ({@link #getCode()}). */
public class STXTException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	/** Error code in UPPERCASE (e.g. {@code INVALID_LINE}). */
	private final String code;

    /**
     * @param code error code in UPPERCASE.
     * @param message descriptive message.
     */
    public STXTException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * @param code error code in UPPERCASE.
     * @param message descriptive message.
     * @param cause original cause.
     */
    public STXTException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /** @return error code in UPPERCASE. */
    public String getCode() { return code; }
    
    @Override
    public String toString() {
        String className = getClass().getSimpleName();
        String message = getLocalizedMessage();
        return (className + "[" + code + "]" + (message != null ? ": " + message : ""));
    }
}
