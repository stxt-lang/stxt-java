package dev.stxt.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.processor.TopDownProcessor;
import test.FileTestLoction;
import test.JSON;

public class TopDownProcessorTest {

	private Node node;

	@BeforeEach
	void loadNode() throws IOException, ParseException {
		Parser parser = new Parser();

		File f = FileTestLoction.getFile("docs/client.stxt");
		List<Node> docs = parser.parseFile(f);
		node = docs.get(0);
		System.out.println("*********************************");
		System.out.println("NODE: " + JSON.toJson(node));
	}

	@Test
	void testNumber() {
		CountProcessorTest count = new CountProcessorTest();
		TopDownProcessor main = new TopDownProcessor(Collections.singletonList(count));
		main.process(node);

		System.out.println("Nodes = " + count.total);
		assertEquals(10, count.total, "Número de nodos");
	}

	@Test
	void testOrder() {
		OrderProcessorTest order = new OrderProcessorTest();
		TopDownProcessor main = new TopDownProcessor(Collections.singletonList(order));
		main.process(node);
		
		List<String> elements = order.elements;
		for (String elem: elements) System.out.println("  => " + elem);
		assertEquals("com.gym.docs:cliente", elements.get(0));
		assertEquals("com.gym.docs:datos personales", elements.get(1));
	}
}


