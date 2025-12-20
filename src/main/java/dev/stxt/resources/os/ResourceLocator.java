package dev.stxt.resources.os;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Localiza recursos STXT (schemas, transforms, etc.) en una lista de roots.
 */
final class ResourceLocator {
	private static final String STXT_EXTENSION = ".stxt";

	private final List<Path> roots;

	ResourceLocator(List<Path> roots) {
		this.roots = Objects.requireNonNull(roots, "roots must not be null");
	}

	Path resolveSchema(String namespace) {
		Objects.requireNonNull(namespace, "namespace must not be null");
		return resolveStxtResource("schema", namespace);
	}

	Path resolveTransformStxt2Xml(String namespace) {
		Objects.requireNonNull(namespace, "namespace must not be null");
		return resolveStxtResource("transform.stxt2xml", namespace);
	}

	private Path resolveStxtResource(String kind, String namespace) {
		String dirName = kindToDirectory(kind);
		String fileName = namespaceToFilename(namespace);

		for (Path root : roots) {
			Path dir = root.resolve(dirName);
			Path candidate = dir.resolve(fileName);
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
		}

		return null;
	}

	private static String kindToDirectory(String kind) {
		if ("schema".equals(kind)) {
			return "@stxt.schema";
		}
		if (kind != null && kind.startsWith("transform.")) {
			String suffix = kind.substring("transform.".length());
			return "@stxt.transform." + suffix;
		}
		// fallback genérico: "@stxt." + kind
		return "@stxt." + kind;
	}

	private static String namespaceToFilename(String namespace) {
		return namespace + STXT_EXTENSION;
	}
}
