package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.STXTException;
import dev.stxt.runtime.STXT;
import test.Corpus;

/**
 * Regresión de validación: los documentos reales de stxt-web deben parsear y validar sin
 * errores contra los schemas/templates del propio stxt-web.
 *
 * A diferencia del equivalente en stxt-vscode, aquí el validador es fail-fast (PENDIENTES,
 * punto 18): no hay lista de errores, así que la comprobación es "no lanza excepción".
 */
public class CorpusDocumentsTest {

	@TestFactory
	List<DynamicTest> validaCadaDocumento() {
		File root = Corpus.findStxtWeb();
		Assumptions.assumeTrue(root != null, "requiere el proyecto hermano stxt-web (usa STXT_WEB=/ruta para indicarlo)");

		Corpus.CorpusLoader loader = Corpus.loadLoader(root);
		List<File> files = Corpus.corpusFiles(root, Corpus.DOC_DIRS);
		assertTrue(files.size() > 0, "no se ha encontrado ningún .stxt en " + Corpus.DOC_DIRS);

		List<DynamicTest> tests = new ArrayList<>();
		for (File file: files) {
			String name = Corpus.relative(root, file);

			tests.add(dynamicTest("valida " + name, () -> {
				List<Node> nodes = STXT.parser(loader).parse(Corpus.read(file));
				assertTrue(nodes.size() > 0, name + " no ha producido ningún nodo");
			}));
		}

		tests.add(dynamicTest("todos los documentos declaran un namespace con schema conocido", () -> {
			// Sin esto los tests de arriba podrían pasar de forma trivial: un documento sin
			// namespace no se contrasta contra ningún schema.
			for (File file: files) {
				for (Node node: STXT.rawParser().parse(Corpus.read(file))) {
					String label = Corpus.relative(root, file) + " → " + node.getName();

					assertNotEquals("", node.getNamespace(), label + ": documento sin namespace");
					assertNotNull(STXT.schemaProvider(loader).getSchema(node.getNamespace()),
							label + ": no hay schema para " + node.getNamespace());
				}
			}
		}));

		return tests;
	}

	/**
	 * Un mismo namespace está descrito en stxt-web dos veces: como schema (`.stxt/schemas/`) y
	 * como template (`.stxt/templates/`). Como el template se compila a Schema, ambos deben
	 * validar los documentos exactamente igual.
	 */
	@TestFactory
	List<DynamicTest> schemaYTemplateValidanIgual() {
		File root = Corpus.findStxtWeb();
		Assumptions.assumeTrue(root != null, "requiere el proyecto hermano stxt-web (usa STXT_WEB=/ruta para indicarlo)");

		Corpus.CorpusLoader fromSchemas = Corpus.loadLoader(root, List.of(".stxt/schemas"));
		Corpus.CorpusLoader fromTemplates = Corpus.loadLoader(root, List.of(".stxt/templates"));

		List<DynamicTest> tests = new ArrayList<>();
		for (File file: Corpus.corpusFiles(root, Corpus.DOC_DIRS)) {
			String name = Corpus.relative(root, file);
			String text = Corpus.read(file);

			// Sólo son comparables los documentos cuyo namespace está descrito de las dos formas
			if (!describedBoth(text, fromSchemas, fromTemplates))
				continue;

			tests.add(dynamicTest("mismo resultado en " + name, () ->
					assertEquals(outcome(text, fromSchemas), outcome(text, fromTemplates),
							name + ": el template y el schema no validan igual")));
		}

		assertTrue(tests.size() > 0, "ningún documento tiene su namespace descrito como schema y como template");
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

	// Resultado comparable de validar: el código del primer error, o "OK" si no hubo ninguno
	private static String outcome(String text, Corpus.CorpusLoader loader) {
		try {
			STXT.parser(loader).parse(text);
			return "OK";
		}
		catch (STXTException e) {
			return "[" + e.getCode() + "]";
		}
	}
}
