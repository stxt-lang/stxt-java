package dev.stxt.exceptions;

/** Envuelve un {@link java.io.IOException} al leer un fichero STXT o de recursos. */
public class STXTIOException extends STXTException {
	private static final long serialVersionUID = 1L;

    /** @param cause excepción de E/S original. */
    public STXTIOException(java.io.IOException cause) {
        super("IO_ERROR", "I/O error: " + cause.getMessage(), cause);
    }
}
