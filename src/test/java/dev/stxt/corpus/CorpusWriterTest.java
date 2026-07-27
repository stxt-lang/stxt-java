package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import dev.stxt.Node;
import dev.stxt.runtime.NodeWriter;
import dev.stxt.runtime.NodeWriter.IndentStyle;
import dev.stxt.runtime.STXT;
import test.Corpus;

/**
 * Regresión del writer: escribir un documento parseado y volver a parsearlo no debe perder ni
 * cambiar nada. Se prueba con los dos estilos de indentación sobre todo el corpus de stxt-web,
 * schemas y documentos incluidos.
 */
public class CorpusWriterTest {

	@TestFactory
	List<DynamicTest> idaYVuelta() {
		File root = Corpus.findStxtWeb();
		Assumptions.assumeTrue(root != null, "requiere el proyecto hermano stxt-web (usa STXT_WEB=/ruta para indicarlo)");

		List<File> files = new ArrayList<>();
		files.addAll(Corpus.corpusFiles(root, Corpus.SCHEMA_DIRS));
		files.addAll(Corpus.corpusFiles(root, Corpus.DOC_DIRS));

		List<DynamicTest> tests = new ArrayList<>();
		for (IndentStyle style: IndentStyle.values()) {
			for (File file: files) {
				String name = Corpus.relative(root, file);

				tests.add(dynamicTest(style + ": estable en " + name, () -> {
					List<Node> original = STXT.rawParser().parse(Corpus.read(file));
					String written = NodeWriter.toSTXT(original, style);

					// Sin esto, un writer que devolviera "" pasaría la comparación de abajo
					assertFalse(written.isBlank(), name + ": el writer no ha producido nada");

					List<Node> reparsed = STXT.rawParser().parse(written);
					assertEquals(original.size(), reparsed.size(),
							name + ": la salida del writer no tiene las mismas raíces");

					assertEquals(written, NodeWriter.toSTXT(reparsed, style),
							name + ": el árbol cambia al reparsear la salida del writer");
				}));
			}
		}

		return tests;
	}
}
