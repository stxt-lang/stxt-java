package dev.stxt.discovery;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.utils.StringUtils;

/**
 * The outcome of resolving a document's definitions (STXT-DISCOVERY-SPEC): the chain of
 * levels, the active definition per namespace (nearest level wins) and every resolution
 * error found along the way.
 *
 * It implements {@link SchemaProvider}, so it can be handed directly to a
 * {@link dev.stxt.schema.SchemaValidator} to validate the document it was resolved for.
 * Like {@link dev.stxt.schema.SchemaProviderCache}, it serves the meta-schemas of the two
 * reserved namespaces itself, so schema and template documents also validate against it.
 */
public final class DiscoveryResult implements SchemaProvider {
	private final List<DiscoveryLevel> levels;
	private final SchemaProvider schemaMeta;
	private final SchemaProvider templateMeta;

	// Built by DiscoveryResolver; not meant to be constructed directly.
	/**
	 * Creates a result over an already loaded chain.
	 *
	 * @param levels loaded levels, highest precedence first.
	 * @param schemaMeta provider of the {@code @stxt.schema} meta-schema.
	 * @param templateMeta provider of the {@code @stxt.template} meta-schema.
	 */
	public DiscoveryResult(List<DiscoveryLevel> levels, SchemaProvider schemaMeta, SchemaProvider templateMeta) {
		this.levels = levels;
		this.schemaMeta = schemaMeta;
		this.templateMeta = templateMeta;
	}

	/**
	 * Resolves the schema that applies to a namespace: the meta-schemas for the two
	 * reserved namespaces, and otherwise the active definition of the nearest level.
	 *
	 * @param namespace namespace whose schema is wanted.
	 * @return the schema of the namespace, or {@code null} if the chain has no definition for it.
	 */
	@Override
	public Schema getSchema(String namespace) {
		if (Schema.SCHEMA_NAMESPACE.equals(namespace)) {
			return schemaMeta.getSchema(namespace);
		}
		if ("@stxt.template".equals(namespace)) {
			return templateMeta.getSchema(namespace);
		}

		DiscoveryDefinition definition = getDefinition(namespace);
		return definition == null ? null : definition.getSchema();
	}

	/**
	 * The active definition of a namespace: the one from the nearest level that defines it
	 * (STXT-DISCOVERY-SPEC section 5), with its provenance.
	 *
	 * @param namespace namespace whose definition is wanted.
	 * @return the active definition, or {@code null} if the chain has none for the namespace.
	 */
	public DiscoveryDefinition getDefinition(String namespace) {
		String key = StringUtils.lowerCase(namespace);

		for (DiscoveryLevel level : levels) {
			// STXT-DISCOVERY-SPEC section 8: a closer conflict leaves the namespace
			// without an active definition instead of falling back to a farther level.
			if (level.conflictedNamespaces.contains(key)) {
				return null;
			}

			DiscoveryDefinition definition = level.definitions.get(key);

			if (definition != null) {
				return definition;
			}
		}

		return null;
	}

	/**
	 * Every active definition of the chain, with per-namespace precedence already applied:
	 * one entry per namespace, from its nearest defining level.
	 *
	 * @return the active definitions, ordered by level (nearest level's definitions first).
	 */
	public List<DiscoveryDefinition> getActiveDefinitions() {
		Set<String> seen = new HashSet<>();
		List<DiscoveryDefinition> result = new ArrayList<>();

		for (DiscoveryLevel level : levels) {
			// Mark conflicts as seen so getActiveDefinitions() has the same semantics
			// as getDefinition(): they block definitions in farther levels.
			seen.addAll(level.conflictedNamespaces);

			for (Map.Entry<String, DiscoveryDefinition> entry : level.definitions.entrySet()) {
				if (seen.add(entry.getKey())) {
					result.add(entry.getValue());
				}
			}
		}

		return result;
	}

	/** {@return the active schemas of the chain (the schemas of {@link #getActiveDefinitions()})} */
	public List<Schema> getAllSchemas() {
		List<Schema> result = new ArrayList<>();

		for (DiscoveryDefinition definition : getActiveDefinitions()) {
			result.add(definition.getSchema());
		}

		return result;
	}

	/** {@return the resolution chain: the loaded level directories, highest precedence first} */
	public List<Path> getChain() {
		List<Path> result = new ArrayList<>();

		for (DiscoveryLevel level : levels) {
			result.add(level.dir);
		}

		return result;
	}

	/** {@return every resolution error found while loading the chain (STXT-DISCOVERY-SPEC section 8)} */
	public List<DiscoveryError> getErrors() {
		List<DiscoveryError> result = new ArrayList<>();

		for (DiscoveryLevel level : levels) {
			result.addAll(level.errors);
		}

		return result;
	}
}
