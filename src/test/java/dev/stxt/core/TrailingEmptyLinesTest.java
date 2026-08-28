package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.processors.Observer;

/** The final empty lines of a block are not content (STXT-SPEC 10.3, 0.15.0). */
public class TrailingEmptyLinesTest {

	private static List<String> lines(String text) {
		return ((TextNode) new Parser().parse(text).get(0)).getTextLines();
	}

	@Test
	void dropsTheFinalEmptyLinesAtEofWhateverTheFinalLineBreaks() {
		assertEquals(List.of("text"), lines("B >>\n\ttext"));
		assertEquals(List.of("text"), lines("B >>\n\ttext\n"));
		assertEquals(List.of("text"), lines("B >>\n\ttext\n\n"));
		assertEquals(List.of("text"), lines("B >>\n\ttext\n\n\t\t\n\n"));
	}

	@Test
	void dropsTheFinalEmptyLinesWhenAShallowerLineClosesTheBlock() {
		List<Node> roots = new Parser().parse("B >>\n\ttext\n\n\t\nC: x\n");
		assertEquals(List.of("text"), ((TextNode) roots.get(0)).getTextLines());
		assertEquals("C", roots.get(1).getName());
	}

	@Test
	void keepsLeadingAndIntermediateEmptyLines() {
		assertEquals(List.of("", "text", "", "more"), lines("B >>\n\n\ttext\n\n\tmore\n\n"));
	}

	@Test
	void aBlockOfOnlyEmptyLinesIsAsEmptyAsABlockWithNoLines() {
		assertEquals(List.of(), lines("B >>\n"));
		assertEquals(List.of(), lines("B >>\n\t\n"));
		assertEquals(List.of(), lines("B >>\n\n\n"));
	}

	@Test
	void stillNotifiesTheObserversOfEveryPhysicalLineOfTheBlock() {
		int[] textLines = { 0 };
		Parser parser = new Parser();
		parser.registerObserver(new Observer() {
			@Override
			public void onCreate(Node node, String line) {
				// not needed
			}

			@Override
			public void onFinish(Node node) {
				// not needed
			}

			@Override
			public void onComment(int lineNumber, String line) {
				// not needed
			}

			@Override
			public void onTextLine(TextNode node, int lineNumber, String lineString, dev.stxt.LineIndent line) {
				textLines[0]++;
			}
		});
		parser.parse("B >>\n\ttext\n\n\n");
		assertEquals(3, textLines[0]);
	}
}
