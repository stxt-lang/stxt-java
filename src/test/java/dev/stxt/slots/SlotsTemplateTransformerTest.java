package dev.stxt.slots;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.runtime.STXT;
import test.FileTestLoction;
import static org.junit.jupiter.api.Assertions.*;

public class SlotsTemplateTransformerTest {
	@Test void testTemplate() {
		Parser parser = STXT.parser(new ResourcesLoaderDirectory(FileTestLoction.getFile("")));
		File email = FileTestLoction.getFile("docs/email.stxt");
		
		List<Node> docs = parser.parseFile(email);
		for (Node n: docs) {
			String text = SlotsTemplateTransformer.transform(n, "Esto es una prueba de envío de {{{ child[from]:text }}} a otro envío {{{ child[to]:text }}}");
			System.out.println("Text = " + text);
			assertNotNull(text);
			assertEquals(text, "Esto es una prueba de envío de John Smith a otro envío Mery Adams");
		}
	}	
}
