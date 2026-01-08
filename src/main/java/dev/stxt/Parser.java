package dev.stxt;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTIOException;
import dev.stxt.processors.Observer;
import dev.stxt.processors.Validator;
import dev.stxt.utils.FileUtils;

public class Parser {
	/** 
	 *  Configuración de procesadores
	 */
	
	private List<Validator> validators;
	private List<Observer> observers;

	public void registerValidator(Validator v) {
        if (validators == null) {
            validators = new ArrayList<Validator>();
        }
        validators.add(v);
	}
	public void registerObserver(Observer o) {
        if (observers == null) {
            observers = new ArrayList<Observer>();
        }
        observers.add(o);
	}

	/** 
	 *  Parseo principal del texto
	 */
	
	public List<Node> parseFile(File srcFile) {
		try {
			return parse(FileUtils.readFileContent(srcFile));
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}
	}

	public List<Node> parse(String content) {
		content = FileUtils.removeUTF8BOM(content);

		ParseState state = new ParseState();
		int lineNumber = 0;

		try (BufferedReader in = new BufferedReader(new StringReader(content))) {
			String line;
			while ((line = in.readLine()) != null) {
				lineNumber++;
				processLine(line, lineNumber, state);
			}
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}

		// Cerrar todos los nodos pendientes al EOF
		closeToLevel(state, 0);
		
		// Retorno documentos
		return state.getDocuments();
	}

	private void processLine(String line, int lineNumber, ParseState state) {
		LineIndent lineIndent = LineIndentParser.parseLine(line, lineNumber, state);
		if (lineIndent == null)
			return;

		int currentLevel = lineIndent.indentLevel;

		ArrayDeque<Node> stack = state.getStack();
		Node lastNode = !stack.isEmpty() ? stack.peek() : null;
		boolean lastNodeText = lastNode != null && lastNode.isTextNode();

		// 1) Si estamos dentro de un nodo texto, y el nivel indica que sigue siendo texto,
		// añadimos línea de texto y no creamos nodo.
		if (lastNodeText && currentLevel >= stack.size()) {
			lastNode.addTextLine(lineIndent.lineWithoutIndent);
			return;
		}

		// 3) Cerramos nodos hasta el nivel actual (esto "finaliza" y adjunta al padre/documentos)
		closeToLevel(state, currentLevel);

		// 4) Creamos el nuevo nodo y lo dejamos "abierto" en la pila (NO lo adjuntamos aún)
		Node parent = stack.isEmpty() ? null : stack.peek();
		Node node = createNode(lineIndent, lineNumber, currentLevel, parent);
		
		// Pasamos a observers
		observeNode(node);

		// Añadimos a stack
		stack.push(node);
	}

	/**
	 * Cierra nodos hasta que stack.size() == targetLevel.
	 * Cada nodo cerrado pasa por: transform -> filter -> validate -> attach.
	 */
	private void closeToLevel(ParseState state, int targetLevel) {
		ArrayDeque<Node> stack = state.getStack();

		while (stack.size() > targetLevel) {
			Node completed = stack.pop();
			finishNode(completed);

			if (stack.isEmpty())	state.addDocument(completed);
			else					stack.peek().addChild(completed);
		}
	}

	/**
	 * 
	 * @param lineIndent
	 * @param lineNumber
	 * @param level
	 * @param parent
	 * @return
	 */
	private Node createNode(LineIndent lineIndent, int lineNumber, int level, Node parent) {
		final String line = lineIndent.lineWithoutIndent;
		String name = null;
		String value = null;
		boolean textNode = false;

		int nodeIndex = line.indexOf(':');
		int textIndex = line.indexOf(">>");
		
		if (nodeIndex == -1 && textIndex == -1)			throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);
		else if (nodeIndex == -1 && textIndex != -1) 	textNode = true;
		else if (nodeIndex != -1 && textIndex == -1) 	textNode = false;
		else if (nodeIndex < textIndex)					textNode = false;
		else if (nodeIndex >= textIndex)				throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);

		if (textNode) {
			name  = line.substring(0, textIndex);
			value = line.substring(textIndex + 2);
		}
		else {
			name  = line.substring(0, nodeIndex);
			value = line.substring(nodeIndex + 1);
		}

		if (textNode &&  !value.trim().isEmpty())
				throw new ParseException(lineNumber, "INLINE_VALUE_NOT_VALID", "Line not valid: " + line);

		// Namespace por defecto: heredado del padre
		NameNamespace nn = NameNamespaceParser.parse(name, parent != null ? parent.getNamespace(): null, lineNumber, line);
		name = nn.getName();
		String namespace = nn.getNamespace();
		
		// Validamos nombre
		if (name.isEmpty())
			throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);

		// Creamos nodo
		return new Node(lineNumber, level, name, namespace, textNode, value);
	}

	// -------------------------------------------
	// Métodos de validación, transformación, etc.
	// -------------------------------------------
	
	private Node observeNode(Node node) {
	    if (observers != null)
	        for (Observer o: observers)
	            o.onCreate(node);
	    
		return node;
	}
	
	private void finishNode(Node node) {
		node.freeze();
		
	    if (observers != null)
	        for (Observer o : observers)
	            o.onFinish(node);

	    if (validators != null)
	        for (Validator v : validators)
	            v.validate(node);
	}	
}
