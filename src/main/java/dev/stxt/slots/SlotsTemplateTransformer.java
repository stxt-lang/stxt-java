package dev.stxt.slots;

import dev.stxt.Node;
import dev.stxt.exceptions.STXTException;

public class SlotsTemplateTransformer {
	private SlotsTemplateTransformer() {
	}

	// TODO Hacer split por salto de línea y mostrar error en línea correcta (node.getLine como offset + línea)
	public static final String transform(Node node, String template) {
		if (template == null) return "";
		
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
		Object result = node;
		String[] operations = expresion.split(":");
		for (String operation: operations) {
			result = executeOperation(operation, result);
		}		
		if (!(result instanceof String))
			throw new STXTException("OPERATION_RESULT_NOT_STRING", "The result of chain of operations must be String");
		return result;
	}

	private static Object executeOperation(String operation, Object object) {
		String param = null;
		int i1 = operation.indexOf("[");
		if (i1 != -1) {
			param = operation.substring(i1+1).trim();
			if (param.endsWith("]")) 	param = param.substring(0, param.length()-1);
			else						throw new STXTException("OPERATION_PARAM_NOT_VALID", operation);
			operation = operation.substring(0, i1);
		}
		
		operation = operation.trim();
		Operation op = OperationRegistry.get(operation);
		if (op == null) throw new STXTException("OPERATION_NOT_FOUND", operation);
		
		return op.execute(object, param);
	}
}
