package dev.stxt;

import static dev.stxt.Constants.EMPTY_NAMESPACE;
import static dev.stxt.utils.StringUtils.compactString;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import dev.stxt.utils.FileUtils;

public class Parser {
	private List<Validator> validators;
	
	public void registerValidator(Validator v)
	{
		if (validators == null) validators = new ArrayList<Validator>();
		validators.add(v);
	}
	
	public List<Node> parseFile(File srcFile) throws STXTIOException, ParseException {
		try {	
			return parse(FileUtils.readFileContent(srcFile));
		}
		catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}
	}

	public List<Node> parse(String content) throws ParseException {
		content = FileUtils.removeUTF8BOM(content);

		ParseState state = new ParseState();
		int lineNumber = 0;

		try (BufferedReader in = new BufferedReader(new StringReader(content));) {
			String line;
			while ((line = in.readLine()) != null) {
				lineNumber++;
				processLine(line, lineNumber, state);
			}
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}

		// Validators
		Stack<Node> stack = state.getStack();
		while (!stack.isEmpty()) {
		    validateNode(stack.peek());
		    stack.pop();
		}
		
		return state.getDocuments();
	}

	private void processLine(String line, int lineNumber, ParseState state) throws ParseException, STXTIOException {
		// Parseamos la línea actual
		LineIndent lineIndent = LineIndent.parseLine(line, lineNumber, state);
		if (lineIndent == null)
			return;

		int currentLevel = lineIndent.indentLevel;

		// Obtenemos valores del estado actual
		Stack<Node> stack = state.getStack();
		Node lastNode = !stack.isEmpty() ? stack.peek() : null;
		boolean lastNodeMultiline = lastNode != null && lastNode.isMultiline();

		// Multilinea añadir si procede
		if (lastNodeMultiline && currentLevel >= stack.size()) {
			lastNode.addTextLine(lineIndent.lineWithoutIndent);
			return;
		}

		// Validamos nivel inválido por encima
		if (currentLevel > stack.size())
			throw new ParseException(lineNumber, "INDENTATION_LEVEL_NOT_VALID",
					"Level of indent incorrect: " + currentLevel);

		// Creamos nodo
		Node node = createNode(lineIndent, lineNumber, currentLevel, lastNode);

		if (currentLevel == 0) {
			state.addDocument(node);
			
			// Clear stack and validate
			while (!stack.isEmpty()) {
			    validateNode(stack.peek());
			    stack.pop();
			}	
			//stack.clear();			
			
			stack.push(node);
		} else {
			while (stack.size() > currentLevel) {
			    validateNode(stack.peek());
			    stack.pop();				
			}

			Node parent = stack.peek();
			parent.getChildren().add(node);
			stack.push(node);
		}
	}

	private void validateNode(Node node) throws ParseException {
	    if (validators != null) {
		    for (Validator v : validators) {
		        v.validate(node);
		    }
	    }
	}
	
	private Node createNode(LineIndent result, int lineNumber, int level, Node parent) throws ParseException {
		String line = result.lineWithoutIndent;
		String name = null;
		String value = null;
		boolean multiline = false;

		int nodeIndex = line.indexOf(':');
		int textIndex = line.indexOf(">>");

		if ((nodeIndex != -1 && textIndex != -1) || (nodeIndex == -1 && textIndex == -1))
			throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);

		// Inline value
		if (nodeIndex != -1) {
			name = compactString(line.substring(0, nodeIndex));
			value = line.substring(nodeIndex + 1).trim();
		}

		// Multiline Text
		if (textIndex != -1) {
			name = compactString(line.substring(0, textIndex));
			value = line.substring(textIndex + 2).trim();
			if (!value.isEmpty())
				throw new ParseException(lineNumber, "INLINE_VALUE_NOT_VALID", "Line not valid: " + line);
			multiline = true;
		}

		// Obtenemos namespace si hay
		String namespace = parent != null ? parent.getNamespace() : EMPTY_NAMESPACE;

		int namespaceIndx = name.indexOf("(");
		int namespaceEnd = name.lastIndexOf(')');

		if (namespaceIndx != -1) {
			if (namespaceEnd <= namespaceIndx + 1)
				throw new ParseException(lineNumber, "INVALID_NAMESPACE_DEF", "Line not valid: " + line);

			namespace = name.substring(namespaceIndx + 1, namespaceEnd).trim();
			if (namespace.isEmpty())
				throw new ParseException(lineNumber, "INVALID_NAMESPACE_DEF", "Line not valid: " + line);

			name = compactString(name.substring(0, namespaceIndx));
		}

		// Validamos nombre
		if (name.isEmpty())
			throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);

		// check namespace
		validateNamespaceFormat(namespace, lineNumber);

		// Creamos node
		Node node = new Node(lineNumber, level, name, namespace, multiline, value);
		return node;
	}

	/**
	 * Valida el namespace lógico.
	 *
	 * Reglas: - Solo minúsculas, dígitos y punto. - Puede empezar opcionalmente por
	 * '@'. - Debe ser una o varias etiquetas estilo dominio separadas por '.':
	 * etiqueta := [a-z0-9]+ ejemplos válidos: "xxx", "xxx.ddd", "zzz.ttt.ooo",
	 * "@xxx", "@xxx.ddd".
	 */
	private void validateNamespaceFormat(String namespace, int lineNumber) throws ParseException {
		// Permitimos namespace vacío (se usa EMPTY_NAMESPACE heredado, etc.)
		if (namespace == null || namespace.isEmpty())
			return;

		// Patrón: opcional '@', luego una o más etiquetas [a-z0-9]+ separadas por '.'
		if (!namespace.matches("^@?[a-z0-9]+(\\.[a-z0-9]+)*$"))
			throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Namespace not valid: " + namespace);
	}
}