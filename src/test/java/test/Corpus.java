package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.STXTIOException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.utils.FileUtils;

/**
 * Helpers for the regression tests against the real corpus of `../stxt-web`.
 *
 * The corpus is deliberately not copied into this repository: stxt-web is the normative
 * source of the language and the tests must fail when the implementation drifts away from
 * the real documents, not from a frozen copy.
 *
 * The corpus is mandatory: if the sibling project is missing, {@link #findStxtWeb()} throws
 * and every corpus suite fails. They are never skipped: a silently skipped corpus once hid a
 * broken locator for days in a sibling port, so "no corpus" is an error, not an assumption.
 */
public final class Corpus {
	private Corpus() {}

	// Folders of stxt-web holding schemas and templates (they get loaded into the loader).
	public static final List<String> SCHEMA_DIRS = List.of(".stxt");

	// Folders of stxt-web holding documents that must validate against those schemas.
	public static final List<String> DOC_DIRS = List.of("docs", "es", "en");

	/**
	 * Locates `stxt-web`. It can be forced through the STXT_WEB environment variable; by
	 * default it is looked up as a sibling project (`../stxt-web` from the root of this repo).
	 *
	 * @return the root of stxt-web, never null.
	 * @throws IllegalStateException if it cannot be found: the corpus is mandatory.
	 */
	public static File findStxtWeb() {
		List<String> candidates = new ArrayList<>();
		candidates.add(System.getenv("STXT_WEB"));
		candidates.add(".." + File.separator + "stxt-web");

		List<String> tried = new ArrayList<>();
		for (String candidate: candidates) {
			if (candidate == null) continue;

			File root = new File(candidate);
			tried.add(root.getAbsolutePath());
			if (new File(root, ".stxt").isDirectory())
				return root;
		}

		throw new IllegalStateException(
			"The corpus of the sibling project stxt-web is required and was not found. Tried: "
			+ tried + ". Clone stxt-lang/stxt-web next to this repository or set STXT_WEB=/path/to/stxt-web.");
	}

	// Every .stxt under a directory, recursively and in a stable order.
	public static List<File> findStxtFiles(File dir) {
		List<File> result = new ArrayList<>();
		if (!dir.isDirectory())
			return result;

		File[] entries = dir.listFiles();
		if (entries == null)
			return result;

		Arrays.sort(entries);
		for (File entry: entries) {
			if (entry.isDirectory())
				result.addAll(findStxtFiles(entry));
			else if (entry.getName().endsWith(".stxt"))
				result.add(entry);
		}

		return result;
	}

	// The .stxt of the given folders, relative to the root of stxt-web.
	public static List<File> corpusFiles(File root, List<String> dirs) {
		List<File> result = new ArrayList<>();
		for (String dir: dirs)
			result.addAll(findStxtFiles(new File(root, dir)));

		return result;
	}

	public static String read(File file) {
		try {
			return FileUtils.readFileContent(file);
		}
		catch (IOException e) {
			throw new STXTIOException(e);
		}
	}

	// Readable path for the test names: relative to the root of stxt-web and using '/'.
	public static String relative(File root, File file) {
		String path = file.getAbsolutePath();
		String prefix = root.getAbsolutePath() + File.separator;
		if (path.startsWith(prefix))
			path = path.substring(prefix.length());

		return path.replace(File.separatorChar, '/');
	}

	/**
	 * In-memory `ResourcesLoader` that indexes schemas and templates by the namespace they
	 * declare, not by their path. That is what makes it possible to load the `.stxt/**` of
	 * stxt-web, whose layout (`schemas/`, `templates/`, `website/`, ...) is not the
	 * `<ns>/<resource>.stxt` that `ResourcesLoaderDirectory` expects.
	 */
	public static final class CorpusLoader implements ResourcesLoader {
		private final Map<String, String> resources = new HashMap<>();
		private final List<File> schemas = new ArrayList<>();
		private final List<File> templates = new ArrayList<>();

		public void addFile(File file) {
			String content = read(file);

			// The namespace of the root says whether it is a schema or a template; its value, the target namespace
			List<Node> nodes = new Parser().parse(content);
			if (nodes.size() != 1)
				throw new IllegalStateException("Expected 1 root node in " + file + ", found " + nodes.size());

			Node root = nodes.get(0);
			String kind = root.getNamespace();
			String target = root.getText();

			if (!"@stxt.schema".equals(kind) && !"@stxt.template".equals(kind))
				throw new IllegalStateException("Not a schema or template: " + file + " (namespace " + kind + ")");

			if (target == null || target.isEmpty())
				throw new IllegalStateException("Missing target namespace in " + file);

			String key = key(kind, target);
			String previous = resources.put(key, content);
			if (previous != null)
				throw new IllegalStateException("Duplicated " + kind + " for namespace " + target + " (" + file + ")");

			if ("@stxt.schema".equals(kind)) schemas.add(file);
			else templates.add(file);
		}

		@Override
		public String retrieve(String namespace, String resource) {
			String content = resources.get(key(namespace, resource));
			if (content == null)
				throw new ResourceNotFoundException(namespace, resource);

			return content;
		}

		// Every indexed target namespace, without repeats and in a stable order
		public List<String> namespaces() {
			List<String> result = new ArrayList<>();
			for (String key: resources.keySet()) {
				String ns = key.substring(key.indexOf('|') + 1);
				if (!result.contains(ns))
					result.add(ns);
			}

			result.sort(null);
			return result;
		}

		// Namespaces having both a schema and a template, to check that they are equivalent
		public List<String> namespacesWithBoth() {
			List<String> result = new ArrayList<>();
			for (String key: resources.keySet()) {
				if (!key.startsWith("@stxt.schema|")) continue;

				String ns = key.substring("@stxt.schema|".length());
				if (resources.containsKey(key("@stxt.template", ns)))
					result.add(ns);
			}

			result.sort(null);
			return result;
		}

		public List<File> getSchemas() {
			return schemas;
		}

		public List<File> getTemplates() {
			return templates;
		}

		private static String key(String namespace, String resource) {
			return namespace.toLowerCase(Locale.ROOT) + "|" + resource.toLowerCase(Locale.ROOT);
		}
	}

	/** Loads every schema/template of `.stxt/**` into a loader. */
	public static CorpusLoader loadLoader(File root) {
		return loadLoader(root, SCHEMA_DIRS);
	}

	/** Same, but restricted to specific folders (to compare schemas against templates). */
	public static CorpusLoader loadLoader(File root, List<String> dirs) {
		CorpusLoader loader = new CorpusLoader();
		for (File file: corpusFiles(root, dirs))
			loader.addFile(file);

		return loader;
	}
}
