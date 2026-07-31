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
 * Writer regression: writing a parsed document out and parsing it back must lose nothing and
 * change nothing. It is exercised with both indentation styles over the whole stxt-web corpus,
 * schemas and documents alike.
 */
public class CorpusWriterTest {

	@TestFactory
	List<DynamicTest> roundTrip() {
		File root = Corpus.findStxtWeb();
		Assumptions.assumeTrue(root != null, "requires the sibling stxt-web project (use STXT_WEB=/path to point at it)");

		List<File> files = new ArrayList<>();
		files.addAll(Corpus.corpusFiles(root, Corpus.SCHEMA_DIRS));
		files.addAll(Corpus.corpusFiles(root, Corpus.DOC_DIRS));

		List<DynamicTest> tests = new ArrayList<>();
		for (IndentStyle style: IndentStyle.values()) {
			for (File file: files) {
				String name = Corpus.relative(root, file);

				tests.add(dynamicTest(style + ": stable in " + name, () -> {
					List<Node> original = STXT.rawParser().parse(Corpus.read(file));
					String written = NodeWriter.toSTXT(original, style);

					// Without this, a writer returning "" would pass the comparison below
					assertFalse(written.isBlank(), name + ": the writer produced nothing");

					List<Node> reparsed = STXT.rawParser().parse(written);
					assertEquals(original.size(), reparsed.size(),
							name + ": the writer output does not have the same roots");

					assertEquals(written, NodeWriter.toSTXT(reparsed, style),
							name + ": the tree changes when reparsing the writer output");
				}));
			}
		}

		return tests;
	}
}
