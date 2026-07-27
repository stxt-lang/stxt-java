package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import dev.stxt.runtime.STXT;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import test.Corpus;

/**
 * Regresión de carga: todos los schemas y templates reales de stxt-web deben parsear, validar
 * contra su meta-schema y transformarse a Schema sin excepción.
 *
 * Cada fichero se carga en un loader propio para que un fallo señale el fichero culpable y no
 * quede enmascarado por los demás.
 */
public class CorpusSchemasTest {

	@TestFactory
	List<DynamicTest> cargaCadaSchemaYTemplate() {
		File root = Corpus.findStxtWeb();
		Assumptions.assumeTrue(root != null, "requiere el proyecto hermano stxt-web (usa STXT_WEB=/ruta para indicarlo)");

		List<File> files = Corpus.corpusFiles(root, Corpus.SCHEMA_DIRS);
		assertTrue(files.size() > 0, "no se ha encontrado ningún .stxt en " + Corpus.SCHEMA_DIRS);

		List<DynamicTest> tests = new ArrayList<>();
		for (File file: files) {
			String name = Corpus.relative(root, file);

			tests.add(dynamicTest("carga " + name, () -> {
				Corpus.CorpusLoader loader = new Corpus.CorpusLoader();
				loader.addFile(file);

				// El namespace destino que declara el propio fichero
				String namespace = loader.namespaces().get(0);
				Schema schema = STXT.schemaProvider(loader).getSchema(namespace);

				assertNotNull(schema, name + ": no ha producido ningún schema");
			}));
		}

		tests.add(dynamicTest("todos juntos se cargan en un único loader", () -> {
			Corpus.CorpusLoader loader = Corpus.loadLoader(root);
			SchemaProvider provider = STXT.schemaProvider(loader);

			for (String namespace: loader.namespaces())
				assertNotNull(provider.getSchema(namespace), "sin schema para " + namespace);
		}));

		return tests;
	}
}
