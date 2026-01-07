package dev.stxt.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateChildParserTest {
	@Test
	void test1(){
		checkLine("(1) TEXT", 1, 1, "TEXT", 0);
		checkLine("(1) @Ingrediente", 1, 1, "@Ingrediente",0);
		checkLine("(*) @Ingrediente", null, null, "@Ingrediente",0);
		checkLine("(+) @Ingrediente", 1, null, "@Ingrediente",0);
		checkLine("(?) @Ingrediente", null, 1, "@Ingrediente",0);
		checkLine("(?) ENUM [high, medium, low]",null,1,"ENUM",3);
	}

	private void checkLine(String string, Integer min, Integer max, String type, int valuesSize) {
		ChildLine cl = ChildLineParser.parse(string, 0);
		System.out.println("cl = " + cl);
		Assertions.assertEquals(min, cl.getMin());
		Assertions.assertEquals(max, cl.getMax());
		Assertions.assertEquals(type, cl.getType());	
		int num = cl.getValues() != null? cl.getValues().length: 0;
		Assertions.assertEquals(valuesSize, num);
	}
}
