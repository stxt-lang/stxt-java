package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import dev.stxt.runtime.STXT;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import test.Corpus;

/**
 * Loading regression: every real schema and template of stxt-lang must parse, validate against
 * its meta-schema and turn into a Schema without throwing.
 *
 * Each file is loaded into its own loader so a failure points at the guilty file instead of
 * being masked by the others.
 */
public class CorpusSchemasTest {

	@TestFactory
	List<DynamicTest> loadsEachSchemaAndTemplate() {
		File root = Corpus.findStxtLang();

		List<File> files = Corpus.corpusFiles(root, Corpus.SCHEMA_DIRS);
		assertTrue(files.size() > 0, "no .stxt found in " + Corpus.SCHEMA_DIRS);

		List<DynamicTest> tests = new ArrayList<>();
		for (File file: files) {
			String name = Corpus.relative(root, file);

			tests.add(dynamicTest("loads " + name, () -> {
				Corpus.CorpusLoader loader = new Corpus.CorpusLoader();
				loader.addFile(file);

				// The target namespace declared by the file itself
				String namespace = loader.namespaces().get(0);
				Schema schema = STXT.schemaProvider(loader).getSchema(namespace);

				assertNotNull(schema, name + ": produced no schema at all");
			}));
		}

		tests.add(dynamicTest("all of them load together into a single loader", () -> {
			Corpus.CorpusLoader loader = Corpus.loadLoader(root);
			SchemaProvider provider = STXT.schemaProvider(loader);

			for (String namespace: loader.namespaces())
				assertNotNull(provider.getSchema(namespace), "no schema for " + namespace);
		}));

		return tests;
	}
}
