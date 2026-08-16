package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.runtime.NodeWriter;
import dev.stxt.runtime.TreeJson;

/** The 0.7.0 node model: two sealed forms, parent links with integrity, derived level, declared vs effective namespace, mutability. */
public class NodeTest {

	// ---------------------------------------------------------------- forms

	@Test
	void twoFormsWithTheirOwnContent() {
		InlineNode inline = new InlineNode("Title", "  Hello  ");
		TextNode text = new TextNode("Body", "line 1\nline 2");

		assertFalse(inline.isTextNode());
		assertEquals("Hello", inline.getValue());
		assertEquals("Hello", inline.getText());

		assertTrue(text.isTextNode());
		assertEquals(List.of("line 1", "line 2"), text.getTextLines());
		assertEquals("line 1\nline 2", text.getText());
	}

	@Test
	void eachFormOwnsOnlyWhatIsReallyItsOwn() throws NoSuchMethodException {
		// Children and lookups live in InlineNode; text lines in TextNode; nothing of that in Node
		assertNotNull(InlineNode.class.getMethod("getChildren"));
		assertNotNull(InlineNode.class.getMethod("getChild", String.class));
		assertNotNull(TextNode.class.getMethod("getTextLines"));
		assertThrows(NoSuchMethodException.class, () -> Node.class.getMethod("getChildren"));
		assertThrows(NoSuchMethodException.class, () -> Node.class.getMethod("getChild", String.class));
		assertThrows(NoSuchMethodException.class, () -> Node.class.getMethod("getValue"));
		assertThrows(NoSuchMethodException.class, () -> Node.class.getMethod("getTextLines"));
		assertThrows(NoSuchMethodException.class, () -> TextNode.class.getMethod("getChildren"));

		// Walking a tree asks for the form
		InlineNode root = new InlineNode("Doc");
		root.addTextNode("Text", "t");
		root.addInlineNode("Inline");
		int inline = 0, text = 0;
		for (Node child : root.getChildren()) {
			if (child instanceof InlineNode) inline++;
			if (child instanceof TextNode) text++;
		}
		assertEquals(1, inline);
		assertEquals(1, text);
	}

	@Test
	void textNodeSplitsAtLfAndCrlfAndKeepsTrailingEmptyLine() {
		TextNode text = new TextNode("Body", "a\r\nb\n");
		assertEquals(List.of("a", "b", ""), text.getTextLines());

		text.setText("x");
		assertEquals(List.of("x"), text.getTextLines());
		text.addTextLine("y");
		text.setTextLines(List.of("p", "q"));
		assertEquals("p\nq", text.getText());
		text.clearText();
		assertEquals("", text.getText());
	}

	@Test
	void textLinesAndChildrenAreReadOnlyViews() {
		TextNode text = new TextNode("Body", "a");
		assertThrows(UnsupportedOperationException.class, () -> text.getTextLines().add("b"));

		InlineNode inline = new InlineNode("Doc");
		assertThrows(UnsupportedOperationException.class, () -> inline.getChildren().add(new InlineNode("X")));
	}

	// ---------------------------------------------------------------- parent links

	@Test
	void addChildLinksBothEndsAndDerivesTheLevel() {
		InlineNode root = new InlineNode("Doc");
		InlineNode child = root.addInlineNode("Child", "v");
		TextNode grandchild = child.addTextNode("Text", "t");

		assertSame(root, child.getParent());
		assertSame(child, grandchild.getParent());
		assertNull(root.getParent());
		assertEquals(0, root.getLevel());
		assertEquals(1, child.getLevel());
		assertEquals(2, grandchild.getLevel());
		assertEquals(List.of(child), root.getChildren());
	}

	@Test
	void aNodeCannotHaveTwoParents() {
		InlineNode a = new InlineNode("A");
		InlineNode b = new InlineNode("B");
		InlineNode child = a.addInlineNode("Child");

		STXTException ex = assertThrows(STXTException.class, () -> b.addChild(child));
		assertEquals("NODE_ALREADY_ATTACHED", ex.getCode());
		assertSame(a, child.getParent(), "the failed add changes nothing");
		assertTrue(b.getChildren().isEmpty());
	}

	@Test
	void removeChildAndDetachUnlinkBothEnds() {
		InlineNode a = new InlineNode("A");
		InlineNode b = new InlineNode("B");
		InlineNode child = a.addInlineNode("Child");

		assertTrue(a.removeChild(child));
		assertNull(child.getParent());
		assertTrue(a.getChildren().isEmpty());
		assertFalse(a.removeChild(child), "not a child any more");
		assertFalse(b.removeChild(child), "never was a child of b");

		b.addChild(child);
		assertSame(b, child.getParent());
		assertTrue(child.detach());
		assertNull(child.getParent());
		assertFalse(child.detach(), "already a root");
	}

	@Test
	void removeChildUsesIdentityNotEquality() {
		InlineNode root = new InlineNode("Doc");
		InlineNode first = root.addInlineNode("Item", "same");
		InlineNode second = root.addInlineNode("Item", "same");

		assertTrue(root.removeChild(second));
		assertEquals(List.of(first), root.getChildren());
		assertSame(root, first.getParent());
	}

	@Test
	void addChildAtIndexAndReorder() {
		InlineNode root = new InlineNode("Doc");
		InlineNode a = root.addInlineNode("A");
		InlineNode c = root.addInlineNode("C");
		InlineNode b = new InlineNode("B");
		root.addChild(1, b);
		assertEquals(List.of(a, b, c), root.getChildren());

		// Move C to the front: detach, then insert
		c.detach();
		root.addChild(0, c);
		assertEquals(List.of(c, a, b), root.getChildren());
	}

	@Test
	void cyclesAreRejected() {
		InlineNode root = new InlineNode("Doc");
		InlineNode child = root.addInlineNode("Child");
		InlineNode grandchild = child.addInlineNode("Grandchild");

		assertEquals("NODE_CYCLE", assertThrows(STXTException.class, () -> root.addChild(root)).getCode());
		// root has a parent? no; but it is an ancestor of grandchild
		STXTException ex = assertThrows(STXTException.class, () -> grandchild.addChild(root));
		assertEquals("NODE_CYCLE", ex.getCode());
	}

	// ---------------------------------------------------------------- namespaces

	@Test
	void effectiveNamespaceIsInheritedThroughTheParentChain() {
		InlineNode root = new InlineNode("Doc", "com.example.docs", "x");
		InlineNode child = root.addInlineNode("Child");
		TextNode text = child.addTextNode("Text", "t");
		InlineNode other = root.addInlineNode("Other", "org.other.ns", null);

		assertEquals("com.example.docs", root.getDeclaredNamespace());
		assertEquals("", child.getDeclaredNamespace());
		assertEquals("com.example.docs", child.getNamespace());
		assertEquals("com.example.docs", text.getNamespace());
		assertEquals("org.other.ns", other.getNamespace());
		assertEquals("com.example.docs:child", child.getQualifiedName());
	}

	@Test
	void changingADeclaredNamespaceChangesTheWholeInheritingSubtree() {
		InlineNode root = new InlineNode("Doc", "com.example.docs", "x");
		InlineNode child = root.addInlineNode("Child");
		InlineNode own = root.addInlineNode("Own", "org.other.ns", null);

		root.setNamespace("com.example.v2");
		assertEquals("com.example.v2", child.getNamespace());
		assertEquals("org.other.ns", own.getNamespace(), "a declared namespace is not affected");

		root.setNamespace(null);
		assertEquals("", root.getNamespace());
		assertEquals("", child.getNamespace());
	}

	@Test
	void movingASubtreeInheritsTheNewParentNamespaceAndDetachingLosesIt() {
		InlineNode a = new InlineNode("A", "com.a.ns", null);
		InlineNode b = new InlineNode("B", "com.b.ns", null);
		InlineNode child = a.addInlineNode("Child");
		assertEquals("com.a.ns", child.getNamespace());

		child.detach();
		assertEquals("", child.getNamespace());

		b.addChild(child);
		assertEquals("com.b.ns", child.getNamespace());
	}

	@Test
	void namespaceIsLowerCasedAndValidated() {
		InlineNode n = new InlineNode("Doc", "Com.Example", null);
		assertEquals("com.example", n.getDeclaredNamespace());
		assertThrows(ParseException.class, () -> n.setNamespace("nodots"));
		assertThrows(ParseException.class, () -> new InlineNode("Doc", "bad namespace", null));
	}

	@Test
	void parsedTreesExposeDeclaredAndEffectiveNamespaces() {
		List<Node> docs = new Parser().parse(String.join("\n",
			"Doc (com.example.docs): x",
			"\tChild: y",
			"\t\tOther (org.other.ns): z",
			"\t\t\tDeep: w",
			""));
		InlineNode doc = (InlineNode) docs.get(0);
		InlineNode child = (InlineNode) doc.getChildren().get(0);
		InlineNode other = (InlineNode) child.getChildren().get(0);
		Node deep = other.getChildren().get(0);

		assertEquals("com.example.docs", doc.getDeclaredNamespace());
		assertEquals("", child.getDeclaredNamespace());
		assertEquals("com.example.docs", child.getNamespace());
		assertEquals("org.other.ns", other.getDeclaredNamespace());
		assertEquals("org.other.ns", deep.getNamespace());
		assertSame(doc, child.getParent());
		assertSame(other, deep.getParent());
		assertEquals(3, deep.getLevel());
	}

	// ---------------------------------------------------------------- name, value, line

	@Test
	void nameValueAndLineAreMutable() {
		InlineNode n = new InlineNode("Título  Largo", "v");
		assertEquals("Título Largo", n.getName());
		assertEquals("título-largo", n.getCanonicalName());
		assertEquals(Node.NO_LINE, n.getLine());

		n.setName("Otro nombre");
		assertEquals("otro-nombre", n.getCanonicalName());
		n.setValue(null);
		assertEquals("", n.getValue());
		n.setLine(42);
		assertEquals(42, n.getLine());

		ParseException ex = assertThrows(ParseException.class, () -> n.setName("Invalid!"));
		assertEquals("INVALID_NODE_NAME", ex.getCode());
		assertEquals("Otro nombre", n.getName(), "the failed rename changes nothing");
	}

	@Test
	void parserSetsTheLineAndCodeBuiltNodesHaveNone() {
		Node parsed = ((InlineNode) new Parser().parse("Doc: x\n\tChild: y\n").get(0)).getChildren().get(0);
		assertEquals(2, parsed.getLine());
		assertEquals(Node.NO_LINE, new TextNode("T").getLine());
	}

	@Test
	@SuppressWarnings("deprecation")
	void normalizedNameIsTheDeprecatedAliasOfCanonicalName() {
		InlineNode n = new InlineNode("Año Nuevo");
		assertEquals("año-nuevo", n.getCanonicalName());
		assertEquals(n.getCanonicalName(), n.getNormalizedName());
	}

	// ---------------------------------------------------------------- lookups

	@Test
	void childLookupsUseTheEffectiveNamespace() {
		InlineNode root = new InlineNode("Doc", "com.example.docs", null);
		root.addInlineNode("Item", "1");
		root.addInlineNode("Item", "2");
		InlineNode foreign = root.addInlineNode("Item", "org.other.ns", "3");
		root.addTextNode("Text", "t");

		assertEquals(2, root.getChildren("item").size());
		assertEquals(List.of(foreign), root.getChildren("Item", "org.other.ns"));
		assertEquals("AMBIGUOUS_CHILD", assertThrows(STXTException.class, () -> root.getChild("Item")).getCode());
		assertNull(root.getChild("Missing"));
		assertEquals("t", root.getChild("Text").getText());
	}

	// ---------------------------------------------------------------- built trees behave like parsed ones

	@Test
	void aTreeBuiltByCodeWritesAndReparsesToTheSameCanonicalTree() {
		InlineNode doc = new InlineNode("Email", "com.example.docs", "Weekly report");
		doc.addInlineNode("From", "ana@example.com");
		InlineNode to = doc.addInlineNode("To");
		to.addInlineNode("Address", "bob@example.com");
		doc.addTextNode("Body", "Hi Bob,\n\nSee attached.\n");
		doc.addInlineNode("Cc", "org.other.ns", "x");

		String written = NodeWriter.toSTXT(doc);
		List<Node> reparsed = new Parser().parse(written);

		assertEquals(TreeJson.toCanonicalJson(List.of(doc)), TreeJson.toCanonicalJson(reparsed));
		assertTrue(written.contains("Email (com.example.docs): Weekly report"));
		assertTrue(written.contains("\tCc (org.other.ns): x"), "the namespace is written where declared");
		assertFalse(written.contains("From (com.example.docs)"), "inherited namespaces are implicit");
	}
}
