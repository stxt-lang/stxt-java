package dev.stxt.exceptions;

/** Excepción raíz de todas las excepciones de STXT. Toda excepción lleva un código en MAYÚSCULAS ({@link #getCode()}). */
public class STXTException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	/** Código de error en MAYÚSCULAS (p. ej. {@code INVALID_LINE}). */
	private final String code;
    
    /**
     * @param code código de error en MAYÚSCULAS.
     * @param message mensaje descriptivo.
     */
    public STXTException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * @param code código de error en MAYÚSCULAS.
     * @param message mensaje descriptivo.
     * @param cause causa original.
     */
    public STXTException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /** @return código de error en MAYÚSCULAS. */
    public String getCode() { return code; }
    
    @Override
    public String toString() {
        String className = getClass().getSimpleName();
        String message = getLocalizedMessage();
        return (className + "[" + code + "]" + (message != null ? ": " + message : ""));
    }
}
