package dev.stxt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Constants of the parser, and the version of STXT-SPEC it implements ({@link #SPEC_VERSION}). */
public final class Constants {
	private Constants() {
	}

	/** Character that starts a comment line (STXT-SPEC 9). */
	public static final char COMMENT_CHAR	= '#';
	/** Number of spaces that make one indentation level (STXT-SPEC 8). */
	public static final int TAB_SPACES 		= 4;
	/** Tab character: one indentation level on its own (STXT-SPEC 8). */
	public static final char TAB 			= '\t';
	/** Space character; with {@link #TAB}, the only blank of the language (STXT-SPEC 4). */
	public static final char SPACE 			= ' ';
	/** Separator between the name and the value of an inline node (STXT-SPEC 5). */
	public static final char SEP_NODE 		= ':';
	/** Marker that opens a text block node (STXT-SPEC 6). */
	public static final String SEP_TEXT_NODE = ">>";
	/** Encoding of STXT documents: always UTF-8 (STXT-SPEC 3). */
	public static final Charset ENCODING	= StandardCharsets.UTF_8;
	/** The empty namespace: a node whose chain of parents declares none (STXT-SPEC 7). */
	public static final String EMPTY_NAMESPACE = "";

	/**
	 * Default maximum open nesting levels (STXT-SPEC 11.2); level 0 is the first. Configure it
	 * per parser with {@link dev.stxt.Parser#setMaxNesting(int)}; -1 disables the limit.
	 */
	public static final int DEFAULT_MAX_NESTING = 100;
	/**
	 * Default maximum length of one input line, indentation included (STXT-SPEC 11.2).
	 * Configure it per parser with {@link dev.stxt.Parser#setMaxLineLength(int)}; -1 disables
	 * the limit.
	 */
	public static final int DEFAULT_MAX_LINE_LENGTH = 10000;
	/**
	 * Default maximum total input consumed (STXT-SPEC 11.2). Configure it per parser with
	 * {@link dev.stxt.Parser#setMaxInputSize(int)}; -1 disables the limit.
	 */
	public static final int DEFAULT_MAX_INPUT_SIZE = 10000000;

	/**
	 * Upper bound of {@code Min}/{@code Max} in a schema and of the numbers of a template
	 * cardinality: 2^32 - 1 (STXT-SCHEMA-SPEC 10, STXT-TEMPLATE-SPEC 7.1). A greater value is
	 * {@code CARDINALITY_NOT_VALID}; "no maximum" is said by omitting {@code Max}.
	 */
	public static final long MAX_CARDINALITY = 4294967295L;

	/**
	 * Version of STXT-SPEC (the base syntax) this library implements; "STXT 1.0" on its own means
	 * this number (STXT-SPEC §1.1). Each specification is versioned independently, so the schema,
	 * template, tree and discovery specs may carry other numbers. It is distinct from the
	 * version of the artifact: the library version changes with every release, this one only
	 * when STXT-SPEC does.
	 */
	public static final String SPEC_VERSION = "1.0";
}
