package dev.stxt.discovery;

/**
 * A resolution error (STXT-DISCOVERY-SPEC section 8).
 *
 * Resolution errors are collected, not thrown: the spec mandates reporting them while
 * allowing the tool to keep loading the remaining definitions, so a resolve pass returns
 * every error it found instead of aborting at the first one.
 */
public final class DiscoveryError {
	/** Two definitions for the same target namespace at the same level (spec 8.1). */
	public static final String DUPLICATE_NAMESPACE = "DISCOVERY_DUPLICATE_NAMESPACE";

	/** A file under a resolution directory that does not parse as STXT (spec 8.2). */
	public static final String NOT_PARSEABLE = "DISCOVERY_NOT_PARSEABLE";

	/** A file whose root node belongs neither to @stxt.schema nor to @stxt.template (spec 8.3). */
	public static final String NOT_A_DEFINITION = "DISCOVERY_NOT_A_DEFINITION";

	/** A definition that does not validate against its meta-schema (spec 8.4). */
	public static final String INVALID_DEFINITION = "DISCOVERY_INVALID_DEFINITION";

	private final String code;
	private final String file;
	private final String message;
	private final String namespace;

	/**
	 * Creates a resolution error with no namespace involved.
	 *
	 * @param code one of the {@code DISCOVERY_*} constants of this class.
	 * @param file full path of the offending file.
	 * @param message human-readable description of the error.
	 */
	public DiscoveryError(String code, String file, String message) {
		this(code, file, message, null);
	}

	/**
	 * Creates a resolution error.
	 *
	 * @param code one of the {@code DISCOVERY_*} constants of this class.
	 * @param file full path of the offending file.
	 * @param message human-readable description of the error.
	 * @param namespace target namespace involved, or {@code null} if the error is not about one.
	 */
	public DiscoveryError(String code, String file, String message, String namespace) {
		this.code = code;
		this.file = file;
		this.message = message;
		this.namespace = namespace;
	}

	/** {@return the {@code DISCOVERY_*} code of this error} */
	public String getCode() {
		return code;
	}

	/** {@return the full path of the offending file} */
	public String getFile() {
		return file;
	}

	/** {@return the human-readable description of the error} */
	public String getMessage() {
		return message;
	}

	/** {@return the target namespace involved, or {@code null} if the error is not about one} */
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String toString() {
		return "DiscoveryError[" + code + "] " + file + ": " + message;
	}
}
