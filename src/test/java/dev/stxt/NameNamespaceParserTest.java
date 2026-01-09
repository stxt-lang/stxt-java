package dev.stxt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NameNamespaceParserTest
{
    @Test
    void testNamespace() {
        assertNull(null);
        
        checkValid("Hola (com.demo)", "Hola", "com.demo");
        checkValid("Hola (@com.demo)", "Hola", "@com.demo");
    }

    private void checkValid(String nameNamespace, String name, String namespace) {
    	System.out.println("Checking: " + nameNamespace);
    	NameNamespace nn = NameNamespaceParser.parse(nameNamespace, "", 0, "full line");
		assertEquals(name, nn.getName());
		assertEquals(namespace, nn.getNamespace());
		System.out.println("OK");
	}
}
