package dev.stxt.schema;

/** Resuelve el {@link Schema} aplicable a un namespace. Implementaciones componibles (caché, recursos, meta-schema...). */
public interface SchemaProvider {
	/**
	 * @param namespace namespace del que se quiere el schema.
	 * @return el schema del namespace, o {@code null} si no hay ninguno para ese namespace.
	 */
	public Schema getSchema(String namespace);
}
