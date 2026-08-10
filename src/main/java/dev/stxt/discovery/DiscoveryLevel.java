package dev.stxt.discovery;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A loaded resolution directory: its definitions indexed by lowercased target namespace.
 * Internal to {@link DiscoveryResolver}/{@link DiscoveryResult}; not part of the public API.
 */
final class DiscoveryLevel {
	final Path dir;
	/** Definitions of the level by lowercased target namespace, conflicts excluded. */
	final Map<String, DiscoveryDefinition> definitions = new HashMap<>();
	/** Namespaces with a same-level conflict; they block fallback to farther levels. */
	final Set<String> conflictedNamespaces = new HashSet<>();
	final List<DiscoveryError> errors = new ArrayList<>();

	DiscoveryLevel(Path dir) {
		this.dir = dir;
	}
}
