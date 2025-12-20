package dev.stxt.resources.os;

import java.nio.file.Path;
import java.util.List;

/**
 * Resolución de rutas para STXT: - Schemas: @stxt.schema/&lt;namespace&gt;.stxt
 * - Transforms: @stxt.transform.stxt2xml/&lt;namespace&gt;.stxt
 *
 * Fuentes de roots (en orden de prioridad): 1. Extra roots pasados en Config 2.
 * STXT_HOME 3. STXT_PATH 4. Roots por defecto según OS (./.stxt, ~/.stxt,
 * /etc/stxt, etc.)
 */
final class PathResolver {
	private final ResourceLocator locator;
	private final List<Path> roots;

	private PathResolver(List<Path> roots) {
		this.roots = roots;
		this.locator = new ResourceLocator(roots);
	}

	public static PathResolver getPathResolver(Config config) {
		List<Path> roots = StxtRootsBuilder.buildStxtRoots(config);
		return new PathResolver(roots);
	}

	public static final class Config {
		private final List<Path> extraRoots;
		private final Path projectRoot;

		public Config(List<Path> extraRoots, Path projectRoot) {
			this.extraRoots = (extraRoots != null) ? extraRoots : List.of();
			this.projectRoot = projectRoot;
		}

		public List<Path> getExtraRoots() {
			return extraRoots;
		}

		public Path getProjectRoot() {
			return projectRoot;
		}

		public static Config empty() {
			return new Config(List.of(), null);
		}
	}

	public Path resolveSchema(String namespace) {
		return locator.resolveSchema(namespace);
	}

	public Path resolveTransformStxt2Xml(String namespace) {
		return locator.resolveTransformStxt2Xml(namespace);
	}

	public List<Path> getRoots() {
		return this.roots;
	}
}
