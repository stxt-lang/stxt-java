package dev.stxt.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;

/** The canonical text form of STXT-TREE-SPEC 11.1. */
public class NodeWriterTest {

	@Test
	void declaresTheNamespaceOnlyWhereItChangesFromTheParents() {
		String source = "Root (COM.A):\n\tChild (com.a): x\n\tOther (com.b): y\n\t\tDeep (com.b): z\n\t\tBack (com.a): w\nPlain: v\n";
		List<Node> nodes = new Parser().parse(source);

		assertEquals("Root (com.a):\n\tChild: x\n\tOther (com.b): y\n\t\tDeep: z\n\t\tBack (com.a): w\n\nPlain: v\n",
			NodeWriter.toSTXT(nodes));
	}

	@Test
	void writesASubtreeAsARootDeclaringItsNamespace() {
		InlineNode root = (InlineNode) new Parser().parse("Root (com.a):\n\tChild: x\n").get(0);
		assertEquals("Child (com.a): x\n", NodeWriter.toSTXT(root.getChildren().get(0)));
	}

	@Test
	void writesAnEmptyBlockLineAsTheIndentationAlone() {
		List<Node> nodes = new Parser().parse("Doc:\n\tBody >>\n\t\tfirst\n\n\t\tlast\n\t\t\n");
		assertEquals("Doc:\n    Body >>\n        first\n        \n        last\n        \n",
			NodeWriter.toSTXT(nodes, NodeWriter.IndentStyle.SPACES_4));
	}
}
