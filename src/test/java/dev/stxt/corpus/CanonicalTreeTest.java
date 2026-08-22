package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.runtime.TreeJson;
import test.Corpus;
import test.JSON;

/** STXT-TREE-SPEC conformance over the shared tree fixtures in stxt-lang. */
public class CanonicalTreeTest {

    @TestFactory
    List<DynamicTest> matchesNormativeTreeCorpus() {
        File root = Corpus.findStxtLang();

        List<File> files = Corpus.corpusFiles(root, List.of("conformance" + File.separator + "tree"));
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamicTest("the tree corpus is not empty", () -> assertFalse(files.isEmpty())));

        for (File file: files) {
            String name = Corpus.relative(root, file);
            File expectedFile = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 5) + ".json");

            tests.add(dynamicTest(name, () -> {
                assertTrue(expectedFile.isFile(), name + ": missing " + expectedFile.getName());

                List<Node> nodes = new Parser().parse(Corpus.read(file));
                assertEquals(JSON.toJsonTree(Corpus.read(expectedFile)), JSON.toJsonTree(TreeJson.toCanonicalJson(nodes)));
            }));
        }

        return tests;
    }
}
