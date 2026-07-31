package dev.stxt.schema;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.SchemaException;

/** {@link SchemaProvider} that wraps a list of providers, trying them in order and caching the result per namespace. */
public final class SchemaProviderCache implements SchemaProvider {
	private final Map<String, Schema> cache = new ConcurrentHashMap<>();
	private final List<SchemaProvider> providers;
	
	/** @param providers providers to try in order until one of them resolves the schema. */
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

		// Load the schema
		Schema result = null;
		
		for (SchemaProvider provider: providers) {
			try {
				result = provider.getSchema(namespace);
				if (result != null) break;
			}
			catch (ResourceNotFoundException e) {
			}
		}
		
		if (result == null)
			throw new SchemaException("NOT_FOUND_SCHEMA", "Not found schema " + namespace);
		
		// Put it in the cache
		cache.put(namespace, result);
		return result;
	}
}
