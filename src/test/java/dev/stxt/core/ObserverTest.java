package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.LineIndent;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.processors.Observer;

/** {@link Observer}: the four streaming events, in order, with the parent already attached (same as the other ports). */
class ObserverTest {

	private static final class Recorder implements Observer {
		final List<String> events = new ArrayList<>();

		@Override
		public void onCreate(Node node, String line) {
			events.add("create " + node.getName() + " " + node.getLevel() + " " + node.getNamespace() + " [" + line + "]");
		}

		@Override
		public void onFinish(Node node) {
			events.add("finish " + node.getName());
		}

		@Override
		public void onComment(int lineNumber, String line) {
			events.add("comment " + lineNumber + " [" + line + "]");
		}

		@Override
		public void onTextLine(TextNode node, int lineNumber, String lineString, LineIndent line) {
			events.add("text " + node.getName() + " " + lineNumber + " [" + line.lineWithoutIndent + "]");
		}
	}

	@Test
	void observersSeeTheStreamingEventsWithTheParentAlreadyAttached() {
		Recorder recorder = new Recorder();
		Parser parser = new Parser();
		parser.registerObserver(recorder);
		parser.parse("# c\nA (com.a.ns): x\n\tT >>\n\t\tline\n\t# closes T\n\tB: y\n");

		assertEquals(List.of(
			"comment 1 [# c]",
			"create A 0 com.a.ns [A (com.a.ns): x]",
			"create T 1 com.a.ns [\tT >>]",
			"text T 4 [line]",
			"finish T",
			"comment 5 [\t# closes T]",
			"create B 1 com.a.ns [\tB: y]",
			"finish B",
			"finish A"), recorder.events);
	}
}
