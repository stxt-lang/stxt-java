package dev.stxt.discovery;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A loaded resolution directory: its definitions indexed by lowercased target namespace, the
 * namespaces in conflict at this level and the errors found while loading it. Built by
 * {@link DiscoveryResolver}; a consumer may also build one to hand a {@link DiscoveryResult} an
 * in-memory chain.
 */
public final class DiscoveryLevel {
	private final Path dir;
	/** Definitions of the level by lowercased target namespace, conflicts excluded. */
	private final Map<String, DiscoveryDefinition> definitions = new HashMap<>();
	/** Namespaces with a same-level conflict; they block fallback to farther levels. */
	private final Set<String> conflictedNamespaces = new HashSet<>();
	private final List<DiscoveryError> errors = new ArrayList<>();

	/**
	 * Creates an empty level.
	 *
	 * @param dir full path of the resolution directory.
	 */
	public DiscoveryLevel(Path dir) {
		this.dir = dir;
	}

	/** {@return the full path of the resolution directory} */
	public Path getDir() {
		return dir;
	}

	/** {@return the definitions of the level by lowercased target namespace, conflicts excluded} */
	public Map<String, DiscoveryDefinition> getDefinitions() {
		return Collections.unmodifiableMap(definitions);
	}

	/** {@return the namespaces with a same-level conflict; they block fallback to farther levels} */
	public Set<String> getConflictedNamespaces() {
		return Collections.unmodifiableSet(conflictedNamespaces);
	}

	/** {@return the resolution errors found while loading this level} */
	public List<DiscoveryError> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	/**
	 * Registers a definition in this level, under its lowercased namespace.
	 *
	 * @param definition definition to add.
	 */
	public void addDefinition(DiscoveryDefinition definition) {
		definitions.put(definition.getNamespace().toLowerCase(java.util.Locale.ROOT), definition);
	}

	/**
	 * Marks a namespace as conflicted at this level (two definitions in the same level, spec
	 * section 8): it has no active definition and blocks farther levels.
	 *
	 * @param namespace namespace in conflict.
	 */
	public void addConflict(String namespace) {
		String key = namespace.toLowerCase(java.util.Locale.ROOT);
		definitions.remove(key);
		conflictedNamespaces.add(key);
	}

	/**
	 * Records a resolution error of this level.
	 *
	 * @param error error to add.
	 */
	public void addError(DiscoveryError error) {
		errors.add(error);
	}
}
