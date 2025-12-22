package dev.stxt;

public class STXTIOException extends STXTException {

	private static final long serialVersionUID = 1L;

    public STXTIOException(java.io.IOException cause) {
        super("IO_ERROR", "I/O error: " + cause.getMessage(), cause);
    }
}
