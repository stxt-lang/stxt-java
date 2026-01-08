package dev.stxt;

import org.junit.jupiter.api.Test;

import dev.stxt.exceptions.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class NameNamespaceParserTest
{
    @Test
    void testNamespace() {
        assertNull(null);
        
        checkValid("Hola (com.demo)", "Hola", "com.demo");
        checkValid("Hola (@com.demo)", "Hola", "@com.demo");
        checkValid("", "","");
    }

    private void checkValid(String nameNamespace, String name, String namespace) {
    	NameNamespace nn = NameNamespaceParser.parse(nameNamespace, "", 0, "full line");
		assertEquals(name, nn.getName());
		assertEquals(namespace, nn.getNamespace());
	}
}
