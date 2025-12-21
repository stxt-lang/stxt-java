package dev.stxt;

public class ParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int line;
	private final String code;

	public ParseException(int line, String code, String message) {
		super(message);
		this.line = line;
		this.code = code;
	}

	public int getLine() {
		return line;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "ParseException{" + "line=" + line + ", code='" + code + '\'' + ", message='" + getMessage() + '\''
				+ '}';
	}
}
