package dev.stxt.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** {@link DiscoveryFileSystem} (0.11.0): the resolver works over an injected, in-memory tree, as in the other ports. */
class InMemoryFileSystemTest {

	/** A tree of files held in a map; directories are the parents of the files. */
	private static final class MemoryFileSystem implements DiscoveryFileSystem {
		private final Map<Path, String> files = new LinkedHashMap<>();
		int reads = 0;

		void put(String path, String content) {
			files.put(Path.of(path), content);
		}

		@Override
		public boolean isDirectory(Path path) {
			return files.keySet().stream().anyMatch(f -> f.getParent() != null && f.getParent().startsWith(path) && !f.equals(path));
		}

		@Override
		public List<DiscoveryEntry> listDirectory(Path path) {
			List<DiscoveryEntry> entries = new ArrayList<>();
			for (Path file : files.keySet()) {
				if (path.equals(file.getParent()))
					entries.add(new DiscoveryEntry(file, file.getFileName().toString(), false));
				else if (file.startsWith(path) && file.getNameCount() > path.getNameCount() + 1) {
					Path sub = path.resolve(file.getName(path.getNameCount()).toString());
					if (entries.stream().noneMatch(e -> e.path().equals(sub)))
						entries.add(new DiscoveryEntry(sub, sub.getFileName().toString(), true));
				}
			}
			return entries;
		}

		@Override
		public String readFile(Path path) throws IOException {
			reads++;
			String content = files.get(path);
			if (content == null) throw new IOException("No such file: " + path);
			return content;
		}
	}

	private static final DiscoveryEnvironment NO_ENV = new DiscoveryEnvironment() {
		@Override public List<String> getStxtPath() { return null; }
		@Override public Path getUserLevelDir() { return null; }
		@Override public Path getSystemLevelDir() { return null; }
	};

	@Test
	void resolvesAChainHeldInMemory() {
		MemoryFileSystem fs = new MemoryFileSystem();
		fs.put("/repo/.stxt/common.stxt", "Template (@stxt.template): com.acme.common\n\tStructure >>\n\t\tCommon (com.acme.common):\n\t\t\tTitle: (1)\n");
		fs.put("/repo/web/.stxt/nested/web.stxt", "Schema (@stxt.schema): com.acme.web\n\tNode: Web\n");
		fs.put("/repo/web/.stxt/readme.txt", "not a definition");

		DiscoveryResolver resolver = new DiscoveryResolver(fs, NO_ENV, DiscoveryResolver.DEFAULT_MAX_ASCENT);
		DiscoveryResult result = resolver.resolve(Path.of("/repo/web/docs"));

		assertEquals(List.of(Path.of("/repo/web/.stxt"), Path.of("/repo/.stxt")), result.getChain());
		assertNotNull(result.getSchema("com.acme.web"));
		assertNotNull(result.getSchema("com.acme.common"));
		assertEquals(Path.of("/repo/web/.stxt/nested/web.stxt"), result.getDefinition("com.acme.web").getFile());
		assertEquals(1, result.getErrors().size());
		assertEquals(DiscoveryError.NOT_A_DEFINITION, result.getErrors().get(0).getCode());
		assertTrue(result.getErrors().get(0).getFile().endsWith("readme.txt"));

		// Levels are cached per directory: a second resolve does not read again
		int reads = fs.reads;
		resolver.resolve(Path.of("/repo/web"));
		assertEquals(reads, fs.reads);
		resolver.clearCache();
		resolver.resolve(Path.of("/repo/web"));
		assertTrue(fs.reads > reads);
	}

	@Test
	void aDirectoryCycleTerminatesInsteadOfOverflowing() {
		// Pathological fs (DISCOVERY-SPEC section 10): /repo/.stxt and every "loop" subdirectory
		// list a single child "loop" that is again a directory — an unbounded chain the bounded
		// descent must not follow forever. Without the depth limit this is a StackOverflowError.
		DiscoveryFileSystem cyclic = new DiscoveryFileSystem() {
			@Override
			public boolean isDirectory(Path path) {
				return path.toString().equals("/repo/.stxt") || path.getFileName().toString().equals("loop");
			}

			@Override
			public List<DiscoveryEntry> listDirectory(Path path) {
				Path loop = path.resolve("loop");
				return List.of(new DiscoveryEntry(loop, "loop", true));
			}

			@Override
			public String readFile(Path path) {
				return "";
			}
		};

		DiscoveryResolver resolver = new DiscoveryResolver(cyclic, NO_ENV, DiscoveryResolver.DEFAULT_MAX_ASCENT);
		DiscoveryResult result = resolver.resolve(Path.of("/repo"));	// terminates, no StackOverflowError

		assertEquals(List.of(Path.of("/repo/.stxt")), result.getChain());
		assertEquals(0, result.getAllSchemas().size());
		assertEquals(0, result.getErrors().size());
	}

	@Test
	void aSubdirectoryThatCannotBeListedIsToleratedAndTheRestLoads() {
		// A listDirectory that throws IOException for one subdirectory must not escape or stop
		// the rest of the level (DISCOVERY-SPEC section 3, section 8): the sibling still loads.
		DiscoveryFileSystem partial = new DiscoveryFileSystem() {
			@Override
			public boolean isDirectory(Path path) {
				String s = path.toString();
				return s.equals("/repo/.stxt") || s.equals("/repo/.stxt/bad");
			}

			@Override
			public List<DiscoveryEntry> listDirectory(Path path) throws IOException {
				String s = path.toString();
				if (s.equals("/repo/.stxt"))
					return List.of(
						new DiscoveryEntry(Path.of("/repo/.stxt/good.stxt"), "good.stxt", false),
						new DiscoveryEntry(Path.of("/repo/.stxt/bad"), "bad", true));
				if (s.equals("/repo/.stxt/bad"))
					throw new IOException("cannot list this directory");
				return List.of();
			}

			@Override
			public String readFile(Path path) {
				return "Template (@stxt.template): com.acme.ok\n\tStructure >>\n\t\tOk (com.acme.ok):\n\t\t\tTitle: (1)\n";
			}
		};

		DiscoveryResolver resolver = new DiscoveryResolver(partial, NO_ENV, DiscoveryResolver.DEFAULT_MAX_ASCENT);
		DiscoveryResult result = resolver.resolve(Path.of("/repo"));	// does not throw

		assertNotNull(result.getSchema("com.acme.ok"), "the listable sibling still loads");
	}

	@Test
	void aResultCanBeBuiltFromLevelsDirectly() {
		DiscoveryLevel level = new DiscoveryLevel(Path.of("/mem/.stxt"));
		assertEquals(Path.of("/mem/.stxt"), level.getDir());
		assertTrue(level.getDefinitions().isEmpty());
		level.addError(new DiscoveryError(DiscoveryError.NOT_A_DEFINITION, "/mem/.stxt/x", "x"));

		DiscoveryResult result = new DiscoveryResult(List.of(level),
			new dev.stxt.schema.SchemaProviderMeta(), new dev.stxt.template.MetaTemplateSchemaProvider());
		assertEquals(List.of(Path.of("/mem/.stxt")), result.getChain());
		assertEquals(1, result.getErrors().size());
		assertNotNull(result.getSchema("@stxt.schema"));
	}
}
