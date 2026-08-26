package dev.stxt.exceptions;

/**
 * A parser limit exceeded (STXT-SPEC 11.2): nesting depth, line length or input size. Unlike
 * any other parse error it aborts the parse: it is emitted and no further input is processed,
 * in every mode, so it is always the last error. Exceeding a limit does not make the document
 * invalid: the same document may parse under higher limits (see the {@code setMax*} methods of
 * {@link dev.stxt.Parser}).
 */
public class LimitException extends ParseException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a limit error located at a line of the document.
	 *
	 * @param line line number where the limit was exceeded.
	 * @param code error code in UPPERCASE ({@code LIMIT_*}).
	 * @param message descriptive message.
	 */
	public LimitException(int line, String code, String message) {
		super(line, code, message);
	}
}
