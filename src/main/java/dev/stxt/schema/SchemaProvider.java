package dev.stxt.schema;

/** Resolves the {@link Schema} that applies to a namespace. Composable implementations (cache, resources, meta-schema...). */
public interface SchemaProvider {
	/**
	 * @param namespace namespace whose schema is wanted.
	 * @return the schema of the namespace, or {@code null} if there is none for that namespace.
	 */
	public Schema getSchema(String namespace);
}
