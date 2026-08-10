package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import dev.stxt.Node;
import dev.stxt.ParseResult;
import dev.stxt.exceptions.ParseException;
import dev.stxt.runtime.STXT;
import test.Corpus;

/**
 * Validation regression: the real documents of stxt-web must parse and validate without
 * errors against the schemas/templates of stxt-web itself.
 *
 * It uses the multi-error mode ({@link dev.stxt.Parser#parseResult(String)}) so a failure
 * shows the full list of errors of the document instead of just the first one, like the
 * counterpart in stxt-vscode does (which compares `result.getErrors()`).
 */
public class CorpusDocumentsTest {

	@TestFactory
	List<DynamicTest> validatesEachDocument() {
		File root = Corpus.findStxtWeb();
		Assumptions.assumeTrue(root != null, "requires the sibling stxt-web project (use STXT_WEB=/path to point at it)");

		Corpus.CorpusLoader loader = Corpus.loadLoader(root);
		List<File> files = Corpus.corpusFiles(root, Corpus.DOC_DIRS);
		assertTrue(files.size() > 0, "no .stxt found in " + Corpus.DOC_DIRS);

		List<DynamicTest> tests = new ArrayList<>();
		for (File file: files) {
			String name = Corpus.relative(root, file);

			tests.add(dynamicTest("validates " + name, () -> {
				ParseResult result = STXT.parser(loader).parseResult(Corpus.read(file));

				assertTrue(result.getErrors().isEmpty(),
						name + " has " + result.getErrors().size() + " error(s):" + describeErrors(result.getErrors()));
				assertTrue(result.getNodes().size() > 0, name + " produced no node at all");
			}));
		}

		tests.add(dynamicTest("every document declares a namespace with a known schema", () -> {
			// Without this the tests above could pass trivially: a document with no namespace
			// is not checked against any schema at all.
			for (File file: files) {
				for (Node node: STXT.rawParser().parse(Corpus.read(file))) {
					String label = Corpus.relative(root, file) + " → " + node.getName();

					assertNotEquals("", node.getNamespace(), label + ": document with no namespace");
					assertNotNull(STXT.schemaProvider(loader).getSchema(node.getNamespace()),
							label + ": there is no schema for " + node.getNamespace());
				}
			}
		}));

		return tests;
	}

	/**
	 * One and the same namespace is described twice in stxt-web: as a schema (`.stxt/schemas/`)
	 * and as a template (`examples/definitions/templates/`). Since the template compiles down to a Schema, both
	 * must validate the documents exactly alike: not just the same first error, but the same
	 * full list of errors (code + line, in the same order).
	 */
	@TestFactory
	List<DynamicTest> schemaAndTemplateValidateAlike() {
		File root = Corpus.findStxtWeb();
		Assumptions.assumeTrue(root != null, "requires the sibling stxt-web project (use STXT_WEB=/path to point at it)");

		Corpus.CorpusLoader fromSchemas = Corpus.loadLoader(root, List.of(".stxt/schemas"));
		Corpus.CorpusLoader fromTemplates = Corpus.loadLoader(root, List.of("examples/definitions/templates"));

		List<DynamicTest> tests = new ArrayList<>();
		for (File file: Corpus.corpusFiles(root, Corpus.DOC_DIRS)) {
			String name = Corpus.relative(root, file);
			String text = Corpus.read(file);

			// Only the documents whose namespace is described both ways are comparable
			if (!describedBoth(text, fromSchemas, fromTemplates))
				continue;

			tests.add(dynamicTest("same result in " + name, () ->
					assertEquals(errorCodes(text, fromTemplates), errorCodes(text, fromSchemas),
							name + ": the template and the schema do not validate alike")));
		}

		assertTrue(tests.size() > 0, "no document has its namespace described both as a schema and as a template");
		return tests;
	}

	private static boolean describedBoth(String text, Corpus.CorpusLoader schemas, Corpus.CorpusLoader templates) {
		List<String> withBothSchema = schemas.namespaces();
		List<String> withBothTemplate = templates.namespaces();

		for (Node node: STXT.rawParser().parse(text)) {
			String ns = node.getNamespace().toLowerCase();
			if (!withBothSchema.contains(ns) || !withBothTemplate.contains(ns))
				return false;
		}

		return true;
	}

	// Comparable list of validation errors: code and line of each one, in order of appearance.
	private static List<String> errorCodes(String text, Corpus.CorpusLoader loader) {
		ParseResult result = STXT.parser(loader).parseResult(text);
		return result.getErrors().stream()
				.map(e -> "[" + e.getCode() + "] line " + e.getLine())
				.collect(Collectors.toList());
	}

	// Readable message for the assert: "\n\t[CODE] line 12: message" for each error.
	private static String describeErrors(List<ParseException> errors) {
		StringBuilder sb = new StringBuilder();
		for (ParseException e: errors)
			sb.append("\n\t[").append(e.getCode()).append("] line ").append(e.getLine()).append(": ").append(e.getMessage());
		return sb.toString();
	}
}
