package dev.stxt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Constants of the parser, and the version of the specifications it implements ({@link #SPEC_VERSION}). */
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
	 * Version of the STXT specifications this library implements (STXT-SPEC, STXT-TREE-SPEC,
	 * STXT-SCHEMA-SPEC, STXT-TEMPLATE-SPEC, STXT-DISCOVERY-SPEC). It is distinct from the
	 * version of the artifact: the library version changes with every release, this one only
	 * when the specifications do.
	 */
	public static final String SPEC_VERSION = "1.0";
}
