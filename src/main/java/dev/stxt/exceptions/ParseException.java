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
     * @param message descriptive message.
     */
    public ParseException(int line, String code, String message) {
        super(code, "Error at line: " + line + ", " + message);
        this.line = line;
    }
    /** {@return the line number of the document where the error was detected} */
    public int getLine() { return line; }
}
