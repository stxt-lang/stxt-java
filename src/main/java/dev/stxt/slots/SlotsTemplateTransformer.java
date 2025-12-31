package dev.stxt.slots;

import dev.stxt.Node;
import dev.stxt.exceptions.STXTException;

public class SlotsTemplateTransformer {
	private SlotsTemplateTransformer() {
	}
	
	public static final String transform(Node node, String template) {
		final String templateIni = template; 
		StringBuilder result = new StringBuilder();
		
		int ini = template.indexOf("{{{");
		int end = -1;
		while (ini != -1) {
			result.append(template.substring(0, ini));
			template = template.substring(ini+3);
			end = template.indexOf("}}}");
			if (end == -1) throw new STXTException("NOT_FOUND_END_STRING", templateIni);
			String expresion = template.substring(0, end);
			template = template.substring(end + 3);
			result.append(evaluate(expresion, node));
			ini = template.indexOf("{{{");
		}
		result.append(template);
		
		return result.toString();
	}

	private static Object evaluate(String expresion, Node node) {
		return "@@@EVAL@@@";
	}
}
