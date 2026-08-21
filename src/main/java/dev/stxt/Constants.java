package dev.stxt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Constants of the parser, and the version of STXT-SPEC it implements ({@link #SPEC_VERSION}). */
public final class Constants {
	private Constants() {
	}

	public static final char COMMENT_CHAR	= '#';
	public static final int TAB_SPACES 		= 4;
	public static final char TAB 			= '\t';
	public static final char SPACE 			= ' ';
	public static final char SEP_NODE 		= ':';
	public static final Charset ENCODING	= StandardCharsets.UTF_8;
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
