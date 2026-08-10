package dev.stxt.discovery;

import java.nio.file.Path;

import dev.stxt.schema.Schema;

/**
 * An active definition: a schema or template that won the per-namespace precedence for a
 * document's resolution chain, together with where it came from.
 */
public final class DiscoveryDefinition {
	private final String namespace;
	private final Schema schema;
	private final Path file;
	private final Path levelDir;

	/**
	 * Creates a definition.
	 *
	 * @param namespace target namespace of the definition, as written in the definition document.
	 * @param schema the compiled schema (templates are compiled to schemas at load time).
	 * @param file full path of the file the definition was read from.
	 * @param levelDir resolution directory (level) the file belongs to.
	 */
	public DiscoveryDefinition(String namespace, Schema schema, Path file, Path levelDir) {
		this.namespace = namespace;
		this.schema = schema;
		this.file = file;
		this.levelDir = levelDir;
	}

	/** {@return the target namespace of the definition} */
	public String getNamespace() {
		return namespace;
	}

	/** {@return the compiled schema (templates are compiled to schemas at load time)} */
	public Schema getSchema() {
		return schema;
	}

	/** {@return the full path of the file the definition was read from} */
	public Path getFile() {
		return file;
	}

	/** {@return the resolution directory (level) the file belongs to} */
	public Path getLevelDir() {
		return levelDir;
	}
}
