package dev.stxt.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateChildParserTest {
	@Test
	void test1(){
		checkLine("(1) TEXT", 1, 1, "TEXT");
		checkLine("(1) @Ingrediente", 1, 1, "@Ingrediente");
		checkLine("(*) @Ingrediente", null, null, "@Ingrediente");
		checkLine("(+) @Ingrediente", 1, null, "@Ingrediente");
		checkLine("(?) @Ingrediente", null, 1, "@Ingrediente");
	}

	private void checkLine(String string, Integer min, Integer max, String type) {
		ChildLine cl = ChildLineParser.parse(string, 0);
		System.out.println("cl = " + cl);
		Assertions.assertEquals(min, cl.getMin());
		Assertions.assertEquals(max, cl.getMax());
		Assertions.assertEquals(type, cl.getType());		
	}
}
