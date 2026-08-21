package dev.stxt.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;

public class TemplateChildParserTest {
	@Test
	void test1(){
		checkLine("(1) TEXT", 1, 1, "TEXT", 0);
		checkLine("(1) @Ingrediente", 1, 1, "@Ingrediente",0);
		checkLine("(*) @Ingrediente", null, null, "@Ingrediente",0);
		checkLine("(+) @Ingrediente", 1, null, "@Ingrediente",0);
		checkLine("(?) @Ingrediente", null, 1, "@Ingrediente",0);
		checkLine("(?) ENUM [high, medium, low]",null,1,"ENUM",3);
		checkLine("(2,3) TEXT", 2, 3, "TEXT", 0);
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

	@Test
	void testNegativeCountRejected() {
		// STXT-TEMPLATE-SPEC 7.1: the cardinality numbers must be non-negative integers
		ParseException ex = Assertions.assertThrows(ParseException.class, () -> ChildLineParser.parse("(-2) TEXT", 5));
		Assertions.assertEquals("CARDINALITY_NOT_VALID", ex.getCode());
	}

	@Test
	void testPlusSuffixNonNumericRejected() {
		ParseException ex = Assertions.assertThrows(ParseException.class, () -> ChildLineParser.parse("(a+) TEXT", 5));
		Assertions.assertEquals("CARDINALITY_NOT_VALID", ex.getCode());
	}

	@Test
	void testMinusSuffixNonNumericRejected() {
		ParseException ex = Assertions.assertThrows(ParseException.class, () -> ChildLineParser.parse("(a-) TEXT", 5));
		Assertions.assertEquals("CARDINALITY_NOT_VALID", ex.getCode());
	}

	@Test
	void testCommaCountWithThreePartsRejected() {
		// STXT-TEMPLATE-SPEC 7.1: (min,max) accepts only two parts
		ParseException ex = Assertions.assertThrows(ParseException.class, () -> ChildLineParser.parse("(1,2,3) TEXT", 5));
		Assertions.assertEquals("CARDINALITY_NOT_VALID", ex.getCode());
	}

	@Test
	void testCommaCountMinGreaterThanMaxRejected() {
		// STXT-TEMPLATE-SPEC 7.1: in (min,max) it must hold that min <= max
		ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> ChildLineParser.parse("(3,1) TEXT", 5));
		Assertions.assertEquals("MIN_GREATER_THAN_MAX", ex.getCode());
	}
}

