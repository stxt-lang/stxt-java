package dev.stxt.runtime;

import java.util.List;

import dev.stxt.exceptions.ParseException;

/**
 * The outcome of {@link Formatter#format}.
 *
 * @param text   the formatted document: the same lines as the source, in the same order, with
 *               the same line ending (CRLF is kept) and with a final newline only where the
 *               source had one.
 * @param errors syntax errors found while parsing, in line order; empty when the document
 *               parses. Formatting never repairs a document, and whether a document with errors
 *               should be reformatted at all is the caller's decision.
 */
public record FormatResult(String text, List<ParseException> errors) {
}
