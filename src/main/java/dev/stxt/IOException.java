package dev.stxt;

public class IOException extends STXTException {

	private static final long serialVersionUID = 1L;

    public IOException(java.io.IOException cause) {
        super("IO_ERROR", "I/O error: " + cause.getMessage(), cause);
    }
}
