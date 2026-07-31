package dev.stxt.exceptions;

/** Wraps a {@link java.io.IOException} raised while reading an STXT or resource file. */
public class STXTIOException extends STXTException {
	private static final long serialVersionUID = 1L;

    /**
     * Wraps an I/O exception raised while reading a file.
     *
     * @param cause original I/O exception.
     */
    public STXTIOException(java.io.IOException cause) {
        super("IO_ERROR", "I/O error: " + cause.getMessage(), cause);
    }
}
