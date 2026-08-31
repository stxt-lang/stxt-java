package dev.stxt.exceptions;

/** Syntax error detected while parsing (lexical/structural phase, not schema). */
public class ParseException extends STXTException {
	private static final long serialVersionUID = 1L;

	/**
	 * Line of an error that has no single source line: it concerns the document as a whole
	 * ({@code SCHEMA_MULTIPLE_ROOTS}) or a condition with no one line to point at
	 * ({@code NODE_DUPLICATED}, {@code CHILD_NOT_DEFINED}). The value 0 is part of the
	 * conformance surface (the kit asserts it); it is not {@link dev.stxt.Node#NO_LINE} (-1),
	 * which marks nodes built programmatically, never errors.
	 */
	public static final int NO_LINE = 0;

	/** Line number of the document where the error was detected, or {@link #NO_LINE}. */
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
