package dev.stxt.exceptions;

/** Syntax error detected while parsing (lexical/structural phase, not schema). */
public class ParseException extends STXTException {
	private static final long serialVersionUID = 1L;
	
	/** Line number of the document where the error was detected. */
	private final int line;

    /**
     * Creates a syntax error located at a line of the document.
     *
     * @param line line number where the error was detected.
     * @param code error code in UPPERCASE.
     * @param message descriptive message; {@link #getMessage()} returns exactly this string,
     *        with no code and no line (since 0.10.0).
     */
    public ParseException(int line, String code, String message) {
        super(code, message);
        this.line = line;
    }
    /** {@return the line number of the document where the error was detected} */
    public int getLine() { return line; }

    /** {@return {@code "[CODE] line N: message"}, the framed form of the error} */
    @Override
    public String toString() {
        return "[" + getCode() + "] line " + line + ": " + getMessage();
    }
}
