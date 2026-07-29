package dev.stxt.exceptions;

/** Error de sintaxis detectado durante el parseo (fase léxica/estructural, no de schema). */
public class ParseException extends STXTException {
	private static final long serialVersionUID = 1L;
	
	/** Número de línea del documento donde se detectó el error. */
	private final int line;
    
    /**
     * @param line número de línea donde se detectó el error.
     * @param code código de error en MAYÚSCULAS.
     * @param message mensaje descriptivo.
     */
    public ParseException(int line, String code, String message) {
        super(code, "Error at line: " + line + ", " + message);
        this.line = line;
    }
    /** @return número de línea del documento donde se detectó el error. */
    public int getLine() { return line; }
}
