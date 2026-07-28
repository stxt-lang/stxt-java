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
 * Regresión de validación: los documentos reales de stxt-web deben parsear y validar sin
 * errores contra los schemas/templates del propio stxt-web.
 *
 * Usa el modo multi-error ({@link dev.stxt.Parser#parseResult(String)}) para que un fallo
 * muestre la lista completa de errores del documento en vez de sólo el primero, igual que el
 * equivalente en stxt-vscode (que compara `result.getErrors()`).
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
				ParseResult result = STXT.parser(loader).parseResult(Corpus.read(file));

				assertTrue(result.getErrors().isEmpty(),
						name + " tiene " + result.getErrors().size() + " error(es):" + describeErrors(result.getErrors()));
				assertTrue(result.getNodes().size() > 0, name + " no ha producido ningún nodo");
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
	 * validar los documentos exactamente igual: no sólo el mismo primer error, la misma lista
	 * completa de errores (código + línea, en el mismo orden).
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
					assertEquals(errorCodes(text, fromTemplates), errorCodes(text, fromSchemas),
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

	// Lista comparable de errores de validar: código y línea de cada uno, en orden de aparición.
	private static List<String> errorCodes(String text, Corpus.CorpusLoader loader) {
		ParseResult result = STXT.parser(loader).parseResult(text);
		return result.getErrors().stream()
				.map(e -> "[" + e.getCode() + "] línea " + e.getLine())
				.collect(Collectors.toList());
	}

	// Mensaje legible para el assert: "\n\t[CODE] línea 12: mensaje" por cada error.
	private static String describeErrors(List<ParseException> errors) {
		StringBuilder sb = new StringBuilder();
		for (ParseException e: errors)
			sb.append("\n\t[").append(e.getCode()).append("] línea ").append(e.getLine()).append(": ").append(e.getMessage());
		return sb.toString();
	}
}
