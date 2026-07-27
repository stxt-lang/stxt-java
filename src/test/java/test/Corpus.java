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
 * Utilidades para los tests de regresión contra el corpus real de `../stxt-web`.
 *
 * No se copia el corpus a este repositorio a propósito: stxt-web es la fuente normativa del
 * lenguaje y los tests deben fallar cuando la implementación se separa de los documentos
 * reales, no de una copia congelada. Si el proyecto hermano no está, los tests se saltan
 * (ver `Assumptions.assumeTrue` en cada suite) para no romper un clon aislado.
 */
public final class Corpus {
	private Corpus() {}

	// Carpetas de stxt-web con schemas y templates (se cargan en el loader).
	public static final List<String> SCHEMA_DIRS = List.of(".stxt");

	// Carpetas de stxt-web con documentos que deben validar contra esos schemas.
	public static final List<String> DOC_DIRS = List.of("docs", "es", "en");

	/**
	 * Localiza `stxt-web`. Se puede forzar con la variable de entorno STXT_WEB; por defecto se
	 * busca como proyecto hermano (`../stxt-web` desde la raíz de este repositorio).
	 */
	public static File findStxtWeb() {
		List<String> candidates = new ArrayList<>();
		candidates.add(System.getenv("STXT_WEB"));
		candidates.add(".." + File.separator + "stxt-web");

		for (String candidate: candidates) {
			if (candidate == null) continue;

			File root = new File(candidate);
			if (new File(root, ".stxt").isDirectory())
				return root;
		}

		return null;
	}

	// Todos los .stxt bajo un directorio, recursivo y en orden estable.
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

	// Los .stxt de las carpetas indicadas, relativas a la raíz de stxt-web.
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

	// Ruta legible en los nombres de test: relativa a la raíz de stxt-web y con '/'.
	public static String relative(File root, File file) {
		String path = file.getAbsolutePath();
		String prefix = root.getAbsolutePath() + File.separator;
		if (path.startsWith(prefix))
			path = path.substring(prefix.length());

		return path.replace(File.separatorChar, '/');
	}

	/**
	 * `ResourcesLoader` en memoria que indexa los schemas y templates por el namespace que
	 * declaran, no por su ruta. Es lo que permite cargar el `.stxt/**` de stxt-web, cuyo layout
	 * (`schemas/`, `templates/`, `website/`, ...) no es el `<ns>/<resource>.stxt` que espera
	 * `ResourcesLoaderDirectory`.
	 */
	public static final class CorpusLoader implements ResourcesLoader {
		private final Map<String, String> resources = new HashMap<>();
		private final List<File> schemas = new ArrayList<>();
		private final List<File> templates = new ArrayList<>();

		public void addFile(File file) {
			String content = read(file);

			// El namespace de la raíz dice si es schema o template; su valor, el namespace destino
			List<Node> nodes = new Parser().parse(content);
			if (nodes.size() != 1)
				throw new IllegalStateException("Expected 1 root node in " + file + ", found " + nodes.size());

			Node root = nodes.get(0);
			String kind = root.getNamespace();
			String target = root.getValue();

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

		// Todos los namespaces destino indexados, sin repetir y en orden estable
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

		// Namespaces que tienen a la vez schema y template, para comprobar que son equivalentes
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

	/** Carga en un loader todos los schemas/templates de `.stxt/**`. */
	public static CorpusLoader loadLoader(File root) {
		return loadLoader(root, SCHEMA_DIRS);
	}

	/** Igual, pero restringido a unas carpetas concretas (para comparar schemas vs templates). */
	public static CorpusLoader loadLoader(File root, List<String> dirs) {
		CorpusLoader loader = new CorpusLoader();
		for (File file: corpusFiles(root, dirs))
			loader.addFile(file);

		return loader;
	}
}
