package dev.stxt.exceptions;

/**
 * Root of every STXT exception. Every exception carries an UPPERCASE code ({@link #getCode()}).
 * <p>
 * Message framing (since 0.10.0, the same in every port): {@link #getMessage()} returns only the
 * description, with no code and no line; the code and, for {@link ParseException}, the line are
 * separate fields, and whoever formats the output composes them. The frame lives in
 * {@link #toString()}: {@code "[CODE] message"} here and {@code "[CODE] line N: message"} in
 * {@link ParseException}.
 */
public class STXTException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	/** Error code in UPPERCASE (e.g. {@code INVALID_LINE}). */
	private final String code;

    /**
     * Creates an exception with an error code and a message.
     *
     * @param code error code in UPPERCASE.
     * @param message descriptive message.
     */
    public STXTException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * Creates an exception with an error code, a message and a cause.
     *
     * @param code error code in UPPERCASE.
     * @param message descriptive message.
     * @param cause original cause.
     */
    public STXTException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /** {@return the error code in UPPERCASE} */
    public String getCode() { return code; }
    
    /** {@return {@code "[CODE] message"}, the framed form of the error} */
    @Override
    public String toString() {
        return "[" + code + "] " + getMessage();
    }
}
