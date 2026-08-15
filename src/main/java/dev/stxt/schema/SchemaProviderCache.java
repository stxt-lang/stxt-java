package dev.stxt.schema;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.stxt.exceptions.SchemaException;

/** {@link SchemaProvider} that wraps a list of providers, trying them in order and caching the result per namespace. */
public final class SchemaProviderCache implements SchemaProvider {
	private final Map<String, Schema> cache = new ConcurrentHashMap<>();
	private final List<SchemaProvider> providers;
	
	/**
	 * Creates a cache over a list of providers.
	 *
	 * @param providers providers to try in order until one of them resolves the schema.
	 */
	public SchemaProviderCache(List<SchemaProvider> providers) {
		this.providers = providers;
	}

	public Schema getSchema(String namespace) {
		// Return from the cache
		if (namespace == null || namespace.isEmpty())
		    throw new SchemaException("NAMESPACE_REQUIRED", "Namespace is required to load schema");

		namespace = namespace.toLowerCase(Locale.ROOT);
		Schema cached = cache.get(namespace);
		if (cached != null)
			return cached;

		// Ask the providers in order; per the SchemaProvider contract none of them throws "not found"
		Schema result = null;
		for (SchemaProvider provider: providers) {
			result = provider.getSchema(namespace);
			if (result != null) break;
		}

		// No provider has it: null, so that SchemaValidator reports SCHEMA_NOT_FOUND (misses are not cached)
		if (result == null)
			return null;

		// Put it in the cache
		cache.put(namespace, result);
		return result;
	}
}
