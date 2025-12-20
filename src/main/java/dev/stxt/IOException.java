package dev.stxt;

public class IOException extends ParseException {

	private static final long serialVersionUID = 1L;

	public IOException(java.io.IOException io) {
		super(0, "IO_EXCEPTION", io.getMessage());
	}

}
