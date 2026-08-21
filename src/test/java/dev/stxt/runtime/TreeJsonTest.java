package dev.stxt.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;

/** {@link TreeJson#toCanonicalTree} (1.0): the canonical tree as plain maps and lists, the source of {@link TreeJson#toCanonicalJson}. */
class TreeJsonTest {

	@Test
	@SuppressWarnings("unchecked")
	void toCanonicalTreeBuildsTheSpecShapeWithInsertionOrder() {
		List<Node> nodes = new Parser().parse("Doc (com.a.b): v\n\tText >>\n\t\tone\n\t\ttwo\n\tChild: x\n");
		List<Map<String, Object>> tree = TreeJson.toCanonicalTree(nodes);

		assertEquals(1, tree.size());
		Map<String, Object> doc = tree.get(0);
		assertEquals(List.of("name", "canonicalName", "namespace", "form", "value", "children"), List.copyOf(doc.keySet()));
		assertEquals("Doc", doc.get("name"));
		assertEquals("com.a.b", doc.get("namespace"));
		assertEquals("inline", doc.get("form"));
		assertEquals("v", doc.get("value"));

		List<Map<String, Object>> children = (List<Map<String, Object>>) doc.get("children");
		Map<String, Object> text = children.get(0);
		assertEquals(List.of("name", "canonicalName", "namespace", "form", "lines"), List.copyOf(text.keySet()));
		assertEquals("block", text.get("form"));
		assertEquals(List.of("one", "two"), text.get("lines"));
		assertEquals("com.a.b", children.get(1).get("namespace"), "the effective namespace is inherited");
	}

	@Test
	void toCanonicalJsonSerializesTheTree() {
		List<Node> nodes = new Parser().parse("A: \"q\"\n");
		assertEquals("""
			[
			  {
			    "name": "A",
			    "canonicalName": "a",
			    "namespace": "",
			    "form": "inline",
			    "value": "\\"q\\"",
			    "children": []
			  }
			]""", TreeJson.toCanonicalJson(nodes));
		assertEquals(TreeJson.toCanonicalJson(nodes), TreeJson.toCanonicalJson(nodes.get(0)));
	}
}
