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
	 * Version of STXT-SPEC (the base syntax) this library implements; "STXT 1.0" on its own means
	 * this number (STXT-SPEC §1.1). Each specification is versioned independently, so the schema,
	 * template, tree and discovery specs may carry other numbers. It is distinct from the
	 * version of the artifact: the library version changes with every release, this one only
	 * when STXT-SPEC does.
	 */
	public static final String SPEC_VERSION = "1.0";
}
