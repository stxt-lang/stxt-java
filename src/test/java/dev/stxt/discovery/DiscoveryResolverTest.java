package dev.stxt.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Conformance tests of {@link DiscoveryResolver} against STXT-DISCOVERY-SPEC, over real
 * temporary directories: chain building (project ascent, user, system, {@code STXT_PATH}),
 * per-namespace precedence and the resolution errors of section 8.
 */
class DiscoveryResolverTest {

	@TempDir
	Path tempDir;

	/** Configurable {@link DiscoveryEnvironment} for the tests. */
	private static class TestEnvironment implements DiscoveryEnvironment {
		private final List<String> stxtPath;
		private final Path userDir;
		private final Path systemDir;

		TestEnvironment() {
			this(null, null, null);
		}

		TestEnvironment(List<String> stxtPath, Path userDir, Path systemDir) {
			this.stxtPath = stxtPath;
			this.userDir = userDir;
			this.systemDir = systemDir;
		}

		@Override
		public List<String> getStxtPath() {
			return stxtPath;
		}

		@Override
		public Path getUserLevelDir() {
			return userDir;
		}

		@Override
		public Path getSystemLevelDir() {
			return systemDir;
		}
	}

	private static String template(String namespace, String rootNode) {
		return "Template (@stxt.template): " + namespace + "\n" +
			"\tStructure >>\n" +
			"\t\t" + rootNode + " (" + namespace + "):\n" +
			"\t\t\tTitle: (1)\n";
	}

	private static String schema(String namespace, String rootNode) {
		return "Schema (@stxt.schema): " + namespace + "\n" +
			"\tNode: " + rootNode + "\n" +
			"\t\tChildren:\n" +
			"\t\t\tChild: Title\n" +
			"\t\t\t\tMin: 1\n" +
			"\t\t\t\tMax: 1\n" +
			"\tNode: Title\n";
	}

	private void write(Path file, String content) {
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Path dir(String... parts) {
		Path p = tempDir;
		for (String part : parts) {
			p = p.resolve(part);
		}
		return p;
	}

	// --- resolution chain (spec section 4) ---------------------------------------------

	@Test
	void collectsEveryAncestorStxtDirNearestFirst() {
		write(dir("repo", ".stxt", "common.stxt"), template("com.acme.common", "Common"));
		write(dir("repo", "web", ".stxt", "web.stxt"), template("com.acme.web", "Web"));
		write(dir("repo", "web", "docs", "doc.stxt"), "irrelevant");

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		List<Path> chain = resolver.resolveChain(dir("repo", "web", "docs"));

		assertEquals(List.of(dir("repo", "web", ".stxt"), dir("repo", ".stxt")), chain);
	}

	@Test
	void appendsUserAndSystemLevelsAfterProjectLevels() {
		write(dir("repo", ".stxt", "a.stxt"), template("com.acme.a", "A"));
		write(dir("home", "ana", ".stxt", "b.stxt"), template("org.ana.b", "B"));
		write(dir("etc", "stxt", "c.stxt"), template("org.corp.c", "C"));

		TestEnvironment env = new TestEnvironment(null, dir("home", "ana", ".stxt"), dir("etc", "stxt"));
		DiscoveryResolver resolver = new DiscoveryResolver(env);

		List<Path> chain = resolver.resolveChain(dir("repo"));

		assertEquals(List.of(dir("repo", ".stxt"), dir("home", "ana", ".stxt"), dir("etc", "stxt")), chain);
	}

	@Test
	void ignoresUserAndSystemDirsThatDoNotExist() {
		write(dir("repo", ".stxt", "a.stxt"), template("com.acme.a", "A"));

		TestEnvironment env = new TestEnvironment(null, dir("home", "ana", ".stxt"), dir("etc", "stxt"));
		DiscoveryResolver resolver = new DiscoveryResolver(env);

		assertEquals(List.of(dir("repo", ".stxt")), resolver.resolveChain(dir("repo")));
	}

	@Test
	void doesNotDuplicateUserLevelWhenAscentAlreadyFoundIt() {
		write(dir("home", "ana", ".stxt", "a.stxt"), template("org.ana.a", "A"));

		TestEnvironment env = new TestEnvironment(null, dir("home", "ana", ".stxt"), null);
		DiscoveryResolver resolver = new DiscoveryResolver(env);

		assertEquals(List.of(dir("home", "ana", ".stxt")), resolver.resolveChain(dir("home", "ana", "notes")));
	}

	@Test
	void documentWithNoLocationStartsAtUserLevel() {
		write(dir("repo", ".stxt", "a.stxt"), template("com.acme.a", "A"));
		write(dir("home", "ana", ".stxt", "b.stxt"), template("org.ana.b", "B"));

		TestEnvironment env = new TestEnvironment(null, dir("home", "ana", ".stxt"), null);
		DiscoveryResolver resolver = new DiscoveryResolver(env);

		assertEquals(List.of(dir("home", "ana", ".stxt")), resolver.resolveChain(null));
	}

	@Test
	void honorsMaxAscentSafeguard() {
		write(dir("a", ".stxt", "x.stxt"), template("com.acme.x", "X"));
		Path deep = dir("a", "b", "c", "d", "e");
		try {
			Files.createDirectories(deep);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment(), 3);

		// The ascent examines a/b/c/d/e, a/b/c/d and a/b/c, and stops before a.
		assertEquals(List.of(), resolver.resolveChain(deep));
	}

	// --- STXT_PATH (spec section 6) -----------------------------------------------------

	@Test
	void stxtPathReplacesTheWholeChain() {
		write(dir("repo", ".stxt", "a.stxt"), template("com.acme.a", "A"));
		write(dir("ci", "defs", "b.stxt"), template("org.ci.b", "B"));

		TestEnvironment env = new TestEnvironment(
			List.of(dir("ci", "defs").toString()), dir("home", "ana", ".stxt"), dir("etc", "stxt"));
		DiscoveryResolver resolver = new DiscoveryResolver(env);

		assertEquals(List.of(dir("ci", "defs")), resolver.resolveChain(dir("repo")));
	}

	@Test
	void stxtPathDefinedButEmptyLeavesTheChainEmpty() {
		write(dir("repo", ".stxt", "a.stxt"), template("com.acme.a", "A"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment(List.of(), null, null));

		assertEquals(List.of(), resolver.resolveChain(dir("repo")));
		assertEquals(0, resolver.resolve(dir("repo")).getAllSchemas().size());
	}

	@Test
	void stxtPathIgnoresNonexistentEntriesAndKeepsOrderAsPrecedence() {
		write(dir("one", "a.stxt"), template("com.acme.doc", "One"));
		write(dir("two", "a.stxt"), template("com.acme.doc", "Two"));

		TestEnvironment env = new TestEnvironment(
			List.of(dir("missing").toString(), dir("one").toString(), dir("two").toString()), null, null);
		DiscoveryResolver resolver = new DiscoveryResolver(env);

		DiscoveryResult result = resolver.resolve(dir("anywhere"));

		assertEquals(List.of(dir("one"), dir("two")), result.getChain());
		assertEquals(dir("one", "a.stxt"), result.getDefinition("com.acme.doc").getFile());
	}

	// --- per-namespace precedence (spec section 5) --------------------------------------

	@Test
	void theNearestLevelWinsForEachNamespace() {
		write(dir("repo", ".stxt", "web.stxt"), template("com.acme.web", "Old"));
		write(dir("repo", "web", ".stxt", "web.stxt"), template("com.acme.web", "New"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		DiscoveryResult result = resolver.resolve(dir("repo", "web"));

		assertEquals(dir("repo", "web", ".stxt", "web.stxt"), result.getDefinition("com.acme.web").getFile());
		assertEquals(0, result.getErrors().size(), "a cross-level duplicate is not an error");
		assertEquals(1, result.getAllSchemas().size());
	}

	@Test
	void differentNamespacesResolveFromDifferentLevels() {
		write(dir("repo", "web", ".stxt", "web.stxt"), template("com.acme.web", "Web"));
		write(dir("repo", ".stxt", "common.stxt"), template("com.acme.common", "Common"));
		write(dir("home", "ana", ".stxt", "personal.stxt"), template("org.ana.notes", "Notes"));

		TestEnvironment env = new TestEnvironment(null, dir("home", "ana", ".stxt"), null);
		DiscoveryResolver resolver = new DiscoveryResolver(env);

		DiscoveryResult result = resolver.resolve(dir("repo", "web"));

		assertEquals(dir("repo", "web", ".stxt"), result.getDefinition("com.acme.web").getLevelDir());
		assertEquals(dir("repo", ".stxt"), result.getDefinition("com.acme.common").getLevelDir());
		assertEquals(dir("home", "ana", ".stxt"), result.getDefinition("org.ana.notes").getLevelDir());
		assertEquals(3, result.getAllSchemas().size());
	}

	@Test
	void templateAtNearerLevelBeatsSchemaAtFartherOne() {
		write(dir("repo", ".stxt", "doc.stxt"), schema("com.acme.doc", "Document"));
		write(dir("repo", "web", ".stxt", "doc.stxt"), template("com.acme.doc", "Document"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		DiscoveryResult result = resolver.resolve(dir("repo", "web"));

		assertEquals(dir("repo", "web", ".stxt", "doc.stxt"), result.getDefinition("com.acme.doc").getFile());
	}

	@Test
	void subdirectoriesOfAResolutionDirBelongToTheSameLevel() {
		write(dir("repo", ".stxt", "sub", "dir", "a.stxt"), template("com.acme.a", "A"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		DiscoveryResult result = resolver.resolve(dir("repo"));

		assertEquals(dir("repo", ".stxt"), result.getDefinition("com.acme.a").getLevelDir());
	}

	// --- resolution errors (spec section 8) ---------------------------------------------

	@Test
	void aSameLevelDuplicateIsAnErrorAndLeavesTheNamespaceWithoutActiveDefinition() {
		write(dir("repo", ".stxt", "one.stxt"), template("com.acme.doc", "One"));
		write(dir("repo", ".stxt", "two.stxt"), schema("com.acme.doc", "Two"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		DiscoveryResult result = resolver.resolve(dir("repo"));

		List<DiscoveryError> errors = result.getErrors();
		assertEquals(1, errors.size());
		assertEquals(DiscoveryError.DUPLICATE_NAMESPACE, errors.get(0).getCode());
		assertEquals("com.acme.doc", errors.get(0).getNamespace());
		assertNull(result.getSchema("com.acme.doc"));
		assertEquals(0, result.getAllSchemas().size());
	}

	@Test
	void aSameLevelDuplicateDoesNotBlockTheFartherLevelsOtherNamespaces() {
		write(dir("repo", ".stxt", "one.stxt"), template("com.acme.doc", "One"));
		write(dir("repo", ".stxt", "two.stxt"), template("com.acme.doc", "Two"));
		write(dir("repo", ".stxt", "other.stxt"), template("com.acme.other", "Other"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		DiscoveryResult result = resolver.resolve(dir("repo"));

		assertNull(result.getSchema("com.acme.doc"));
		assertNotNull(result.getSchema("com.acme.other"), "the non-conflicting namespace keeps working");
	}

	@Test
	void aNearerSameLevelConflictDoesNotFallBackToAFartherDefinition() {
		write(dir("repo", ".stxt", "one.stxt"), template("com.acme.doc", "One"));
		write(dir("repo", ".stxt", "two.stxt"), template("com.acme.doc", "Two"));
		write(dir("home", "ana", ".stxt", "farther.stxt"), template("com.acme.doc", "Farther"));

		TestEnvironment env = new TestEnvironment(null, dir("home", "ana", ".stxt"), null);
		DiscoveryResolver resolver = new DiscoveryResolver(env);
		DiscoveryResult result = resolver.resolve(dir("repo"));

		assertNull(result.getDefinition("com.acme.doc"));
		assertNull(result.getSchema("com.acme.doc"));
		assertTrue(result.getActiveDefinitions().stream().noneMatch(d -> d.getNamespace().equals("com.acme.doc")));
	}

	@Test
	void aFileThatDoesNotParseIsNotParseable() {
		write(dir("repo", ".stxt", "broken.stxt"), "This line has no colon and no block marker\n");

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		List<DiscoveryError> errors = resolver.resolve(dir("repo")).getErrors();

		assertEquals(1, errors.size());
		assertEquals(DiscoveryError.NOT_PARSEABLE, errors.get(0).getCode());
	}

	@Test
	void aDocumentOfAnotherNamespaceIsNotADefinition() {
		write(dir("repo", ".stxt", "doc.stxt"), "Document (com.acme.doc):\n\tTitle: Hello\n");

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		List<DiscoveryError> errors = resolver.resolve(dir("repo")).getErrors();

		assertEquals(1, errors.size());
		assertEquals(DiscoveryError.NOT_A_DEFINITION, errors.get(0).getCode());
	}

	@Test
	void aNonStxtFileIsNotADefinition() {
		write(dir("repo", ".stxt", "README.md"), "# Not a definition\n");
		write(dir("repo", ".stxt", "good.stxt"), template("com.acme.doc", "Doc"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		DiscoveryResult result = resolver.resolve(dir("repo"));

		assertEquals(1, result.getErrors().size());
		assertEquals(DiscoveryError.NOT_A_DEFINITION, result.getErrors().get(0).getCode());
		assertNotNull(result.getSchema("com.acme.doc"), "the valid definition still loads");
	}

	@Test
	void aDefinitionThatFailsItsMetaSchemaIsInvalidDefinition() {
		write(dir("repo", ".stxt", "bad.stxt"), "Schema (@stxt.schema): com.acme.bad\n\tBogus: not allowed here\n");

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		List<DiscoveryError> errors = resolver.resolve(dir("repo")).getErrors();

		assertEquals(1, errors.size());
		assertEquals(DiscoveryError.INVALID_DEFINITION, errors.get(0).getCode());
	}

	// --- DiscoveryResult as SchemaProvider -----------------------------------------------

	@Test
	void servesTheMetaSchemasOfTheTwoReservedNamespaces() {
		write(dir("repo", ".stxt", "a.stxt"), template("com.acme.a", "A"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		DiscoveryResult result = resolver.resolve(dir("repo"));

		assertNotNull(result.getSchema("@stxt.schema"), "meta-schema of schemas");
		assertNotNull(result.getSchema("@stxt.template"), "meta-schema of templates");
	}

	@Test
	void clearCacheForcesAReload() {
		Path file = dir("repo", ".stxt", "a.stxt");
		write(file, template("com.acme.a", "Old"));

		DiscoveryResolver resolver = new DiscoveryResolver(new TestEnvironment());
		assertNotNull(resolver.resolve(dir("repo")).getSchema("com.acme.a").getNodeDefinition("old"));

		write(file, template("com.acme.a", "New"));
		resolver.clearCache();

		DiscoveryResult reloaded = resolver.resolve(dir("repo"));
		assertNotNull(reloaded.getSchema("com.acme.a").getNodeDefinition("new"));
	}
}
