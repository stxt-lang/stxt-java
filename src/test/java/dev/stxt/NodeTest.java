package dev.stxt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import test.JSON;

class NodeTest {
	@Test
	void toJson_minimalNode_hasEmptyArraysAndNoOptionalFields() {
		Node node = new Node(1, 0, "Title", null, false, "");

		JsonNode json = JSON.toJsonTree(node);
		System.out.println("JSON: " + json.toString());

		// Campos básicos
		assertEquals("title", json.get("name").asText());
		assertEquals(1, json.get("line").asInt());
		assertEquals(0, json.get("level").asInt());

		// Opcionales
		assertTrue(json.get("namespace").isNull());
		assertEquals("", json.get("inline_text").asText());

		// Arrays siempre presentes y vacíos
		JsonNode text = json.get("multiline_text");
		assertTrue(text.isArray());
		assertEquals(0, text.size());

		JsonNode children = json.get("children");
		assertTrue(children.isArray());
		assertEquals(0, children.size());
	}

	@Test
	void toJson_fullNodeWithTextAndChild_isCorrect() {
		Node parent = new Node(1, 0, "Document", "dev.stxt.example", true, "");
		parent.getMultilineText().add("Line 1");
		parent.getMultilineText().add("Line 2");

		Node child = new Node(2, 1, "Title", null, false, "Hello");
		parent.getChildren().add(child);

		JsonNode json = JSON.toJsonTree(parent);
		System.out.println("JSON: " + json.toString());

		// Campos básicos
		assertEquals("document", json.get("name").asText());
		assertEquals("dev.stxt.example", json.get("namespace").asText());
		assertEquals("", json.get("inline_text").asText());
		assertEquals(1, json.get("line").asInt());
		assertEquals(0, json.get("level").asInt());
		assertTrue(json.get("multiline").asBoolean());

		// Text
		JsonNode text = json.get("multiline_text");
		assertEquals(2, text.size());
		assertEquals("Line 1", text.get(0).asText());
		assertEquals("Line 2", text.get(1).asText());

		// Children
		JsonNode childs = json.get("children");
		assertEquals(1, childs.size());

		JsonNode childJson = childs.get(0);
		assertEquals("title", childJson.get("name").asText());
		assertEquals("Hello", childJson.get("inline_text").asText());
		assertEquals(2, childJson.get("line").asInt());
		assertEquals(1, childJson.get("level").asInt());

		// El hijo también debe tener siempre arrays text / children
		assertTrue(childJson.get("multiline_text").isArray());
		assertEquals(0, childJson.get("multiline_text").size());
		assertTrue(childJson.get("children").isArray());
		assertEquals(0, childJson.get("children").size());
	}

}