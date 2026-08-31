package dev.stxt.discovery;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTIOException;
import dev.stxt.schema.DefinitionCompiler;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaParser;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaProviderMeta;
import dev.stxt.template.MetaTemplateSchemaProvider;
import dev.stxt.template.TemplateParser;
import dev.stxt.utils.StringUtils;

/**
 * Reference implementation of STXT-DISCOVERY-SPEC: builds the resolution chain of a
 * document (project ascent, user level, system level, or the {@code STXT_PATH} override),
 * loads every definition of every level and applies the per-namespace precedence.
 *
 * Loaded levels are cached by directory: resolving many documents that share levels loads
 * each directory once, which is the sharing that STXT-DISCOVERY-SPEC section 7 allows — a
 * level's content does not depend on which document is being resolved. Call
 * {@link #clearCache()} when the underlying files may have changed.
 */
public final class DiscoveryResolver {
	/** Name of the resolution directories (STXT-DISCOVERY-SPEC section 3). */
	public static final String STXT_DIR = ".stxt";

	/** File extension of STXT documents. */
	public static final String STXT_EXTENSION = ".stxt";

	/**
	 * Default maximum number of ancestor directories examined during the project-level
	 * ascent (STXT-DISCOVERY-SPEC section 4.1, a safeguard against pathological paths).
	 */
	public static final int DEFAULT_MAX_ASCENT = 32;

	/**
	 * Maximum depth of the recursive descent inside a resolution directory. A safeguard
	 * against symlink loops and pathological trees, analogous to the ascent limit
	 * (STXT-DISCOVERY-SPEC section 3 and section 10): the descent stops at this depth instead
	 * of recursing without bound. Internal, deliberately not exposed through the constructors.
	 */
	private static final int DEFAULT_MAX_DESCENT = 32;

	private final DiscoveryFileSystem fs;
	private final DiscoveryEnvironment env;
	private final int maxAscent;
	private final SchemaProvider schemaMeta = new SchemaProviderMeta();
	private final SchemaProvider templateMeta = new MetaTemplateSchemaProvider();
	private final Map<Path, DiscoveryLevel> levelCache = new LinkedHashMap<>();

	/** Creates a resolver over the real file system and process environment, with the default ascent limit. */
	public DiscoveryResolver() {
		this(new SystemDiscoveryEnvironment(), DEFAULT_MAX_ASCENT);
	}

	/**
	 * Creates a resolver over a given environment, with the default ascent limit.
	 *
	 * @param env environment access ({@code STXT_PATH}, user and system directories).
	 */
	public DiscoveryResolver(DiscoveryEnvironment env) {
		this(env, DEFAULT_MAX_ASCENT);
	}

	/**
	 * Creates a resolver.
	 *
	 * @param env environment access ({@code STXT_PATH}, user and system directories).
	 * @param maxAscent maximum number of ancestor directories examined during the
	 *        project-level ascent.
	 */
	public DiscoveryResolver(DiscoveryEnvironment env, int maxAscent) {
		this(new NioDiscoveryFileSystem(), env, maxAscent);
	}

	/**
	 * Creates a resolver over an arbitrary file system (an in-memory tree, a ZIP, a virtual
	 * workspace), as in the other ports.
	 *
	 * @param fs file-system access (directory checks, listings and file reads).
	 * @param env environment access ({@code STXT_PATH}, user and system directories).
	 * @param maxAscent maximum number of ancestor directories examined during the
	 *        project-level ascent.
	 */
	public DiscoveryResolver(DiscoveryFileSystem fs, DiscoveryEnvironment env, int maxAscent) {
		this.fs = fs;
		this.env = env;
		this.maxAscent = maxAscent;
	}

	/**
	 * Builds the resolution chain of a document (STXT-DISCOVERY-SPEC sections 4 and 6)
	 * without loading any definition.
	 *
	 * @param documentDir directory containing the document, or {@code null} for a document
	 *        with no file-system location (standard input, an unsaved buffer), whose chain
	 *        starts at the user level.
	 * @return the existing resolution directories, highest precedence first.
	 */
	public List<Path> resolveChain(Path documentDir) {
		// STXT_PATH, when defined, completely replaces the chain (spec section 6).
		List<String> stxtPath = env.getStxtPath();

		if (stxtPath != null) {
			return existingUnique(stxtPath.stream().map(Path::of).toList());
		}

		List<Path> chain = new ArrayList<>();

		// Project level: every .stxt directory from the document's directory upward.
		if (documentDir != null) {
			Path dir = documentDir;

			for (int level = 0; level < maxAscent && dir != null; level++) {
				Path candidate = dir.resolve(STXT_DIR);

				if (fs.isDirectory(candidate)) {
					chain.add(candidate);
				}

				dir = dir.getParent();
			}
		}

		// User and system levels. The ascent may have reached them already (a document
		// under the user's home finds $HOME/.stxt as a project candidate): deduplicate.
		for (Path dir : Arrays.asList(env.getUserLevelDir(), env.getSystemLevelDir())) {
			if (dir != null && !chain.contains(dir) && fs.isDirectory(dir)) {
				chain.add(dir);
			}
		}

		return chain;
	}

	/**
	 * Resolves the definitions applicable to a document: builds its chain, loads every
	 * level (from the cache when already loaded) and returns the result with the
	 * per-namespace precedence applied.
	 *
	 * @param documentDir directory containing the document, or {@code null} for a document
	 *        with no file-system location.
	 * @return the resolution result, usable directly as a {@link SchemaProvider}.
	 */
	public DiscoveryResult resolve(Path documentDir) {
		List<Path> chain = resolveChain(documentDir);
		List<DiscoveryLevel> levels = new ArrayList<>();

		for (Path dir : chain) {
			levels.add(loadLevel(dir));
		}

		return new DiscoveryResult(levels, schemaMeta, templateMeta);
	}

	/**
	 * Empties the level cache, so that the next resolve re-reads every directory. Call it
	 * when the definition files may have changed (e.g. from a file watcher).
	 */
	public void clearCache() {
		levelCache.clear();
	}

	// Filters a list of directories down to the existing ones, removing duplicates.
	private List<Path> existingUnique(List<Path> dirs) {
		List<Path> result = new ArrayList<>();

		for (Path dir : dirs) {
			if (!result.contains(dir) && fs.isDirectory(dir)) {
				result.add(dir);
			}
		}

		return result;
	}

	// Loads a resolution directory (or returns it from the cache): every file under it,
	// recursively, with the level-local duplicate detection of spec section 5.
	private DiscoveryLevel loadLevel(Path dir) {
		DiscoveryLevel cached = levelCache.get(dir);

		if (cached != null) {
			return cached;
		}

		DiscoveryLevel level = new DiscoveryLevel(dir);

		for (Path file : collectFiles(dir)) {
			loadFile(file, level);
		}

		levelCache.put(dir, level);
		return level;
	}

	// Collects every file under a directory, recursively, sorted by path so that results
	// and error messages do not depend on the listing order of the file system. The descent
	// is bounded by DEFAULT_MAX_DESCENT and tolerant of listing failures (STXT-DISCOVERY-SPEC
	// section 3 and section 10): a subdirectory that reaches the depth limit or cannot be
	// listed simply contributes no files, never an exception. Together with adapters that do
	// not follow directory symlinks, this stops symlink loops and pathological trees from
	// turning resolution into unbounded recursion.
	private List<Path> collectFiles(Path dir) {
		List<Path> files = new ArrayList<>();
		collectFiles(dir, files, 0);
		files.sort(Comparator.naturalOrder());
		return files;
	}

	private void collectFiles(Path dir, List<Path> files, int depth) {
		// Safeguard against symlink loops and pathological trees: stop descending.
		if (depth >= DEFAULT_MAX_DESCENT)
			return;

		List<DiscoveryEntry> entries;
		try {
			entries = fs.listDirectory(dir);
		} catch (IOException | STXTIOException e) {
			// A directory that cannot be listed contributes no files (section 3); it does not
			// stop the resolution of the rest of the level, and the error does not escape.
			return;
		}

		for (DiscoveryEntry entry : entries) {
			if (entry.isDirectory())	collectFiles(entry.path(), files, depth + 1);
			else						files.add(entry.path());
		}
	}

	// Loads one file of a level: parses it and registers every root as a definition,
	// reporting the errors of spec section 8.
	private void loadFile(Path file, DiscoveryLevel level) {
		// Spec section 3: every file under a resolution directory must be a definition.
		if (!file.toString().endsWith(STXT_EXTENSION)) {
			level.addError(new DiscoveryError(
				DiscoveryError.NOT_A_DEFINITION, file.toString(),
				"Not an STXT definition file: " + file));
			return;
		}

		String content;

		try {
			content = fs.readFile(file);
		} catch (IOException e) {
			level.addError(new DiscoveryError(
				DiscoveryError.NOT_PARSEABLE, file.toString(),
				"Cannot read " + file + ": " + e.getMessage()));
			return;
		}

		List<Node> nodes;

		try {
			nodes = new Parser().parse(content);
		} catch (RuntimeException e) {
			level.addError(new DiscoveryError(
				DiscoveryError.NOT_PARSEABLE, file.toString(),
				"Cannot parse " + file + ": " + e.getMessage()));
			return;
		}

		if (nodes.isEmpty()) {
			level.addError(new DiscoveryError(
				DiscoveryError.NOT_A_DEFINITION, file.toString(),
				"Empty document, not a definition: " + file));
			return;
		}

		for (Node node : nodes) {
			loadRootNode(node, file, level);
		}
	}

	// Validates one root node against its meta-schema, compiles it to a schema and
	// registers it in the level, detecting same-level duplicates.
	private void loadRootNode(Node node, Path file, DiscoveryLevel level) {
		String namespace = node.getNamespace();
		Schema schema;

		try {
			if (Schema.TEMPLATE_NAMESPACE.equals(namespace)) {
				schema = DefinitionCompiler.compileNode(node, templateMeta, TemplateParser::transformNodeToSchema);
			} else if (Schema.SCHEMA_NAMESPACE.equals(namespace)) {
				schema = DefinitionCompiler.compileNode(node, schemaMeta, SchemaParser::transformNodeToSchema);
			} else {
				level.addError(new DiscoveryError(
					DiscoveryError.NOT_A_DEFINITION, file.toString(),
					"Root node belongs to '" + namespace + "', not to @stxt.schema or @stxt.template: " + file));
				return;
			}
		} catch (RuntimeException e) {
			String message = e instanceof ParseException pe ? "[" + pe.getCode() + "] " + pe.getMessage() : String.valueOf(e.getMessage());
			level.addError(new DiscoveryError(
				DiscoveryError.INVALID_DEFINITION, file.toString(),
				"Invalid definition in " + file + ": " + message));
			return;
		}

		String key = StringUtils.lowerCase(schema.getNamespace());
		DiscoveryDefinition existing = level.getDefinitions().get(key);

		// Spec section 8: on a same-level duplicate, never silently pick one of the
		// definitions — the namespace has no active definition while the conflict exists.
		if (level.getConflictedNamespaces().contains(key) || existing != null) {
			level.addConflict(schema.getNamespace());

			String firstFile = existing != null ? existing.getFile().toString() : "another file of this level";
			level.addError(new DiscoveryError(
				DiscoveryError.DUPLICATE_NAMESPACE, file.toString(),
				"Duplicate definition for namespace '" + schema.getNamespace() + "' at level " + level.getDir() + ": " +
				"already defined in " + firstFile,
				schema.getNamespace()));
			return;
		}

		level.addDefinition(new DiscoveryDefinition(schema.getNamespace(), schema, file, level.getDir()));
	}

	// Compilation itself is the shared pipeline of DefinitionCompiler (compileNode):
	// validate against the meta of the kind, throw the first error, transform.
}
