package dev.stxt;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTIOException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.processors.Observer;
import dev.stxt.processors.Validator;
import dev.stxt.utils.FileUtils;

/**
 * Motor de parseo de STXT línea a línea. No conoce los schemas: la validación semántica se
 * engancha mediante {@link #registerValidator(Validator)} y {@link #registerObserver(Observer)}.
 * Ver {@link dev.stxt.runtime.STXT} como fachada de uso habitual, ya configurada con validación.
 */
public class Parser {
	private List<Validator> validators;
	private List<Observer> observers;

	/** @param v el {@link Validator} a registrar, invocado al cerrar cada nodo durante el parseo. */
	public void registerValidator(Validator v) {
        if (validators == null) {
            validators = new ArrayList<>();
        }
        validators.add(v);
	}
	/** @param o el {@link Observer} a registrar, notificado al abrir y cerrar cada nodo durante el parseo. */
	public void registerObserver(Observer o) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(o);
	}

	/**
	 * Igual que {@link #parse(String)} pero leyendo el contenido de un fichero.
	 *
	 * @param srcFile fichero con el documento STXT.
	 * @return los nodos raíz del documento parseado.
	 * @throws STXTIOException si el fichero no se puede leer.
	 */
	public List<Node> parseFile(File srcFile) {
		try {
			return parse(FileUtils.readFileContent(srcFile));
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}
	}

	/**
	 * Igual que {@link #parseResult(String)} pero leyendo el contenido de un fichero.
	 *
	 * @param srcFile fichero con el documento STXT.
	 * @return el resultado del parseo en modo multi-error.
	 * @throws STXTIOException si el fichero no se puede leer.
	 */
	public ParseResult parseResultFile(File srcFile) {
		try {
			return parseResult(FileUtils.readFileContent(srcFile));
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}
	}

	/**
	 * Modo tradicional fail-fast: lanza la primera excepción encontrada (de sintaxis o de
	 * validación), sin seguir recorriendo el resto del documento (importante en documentos muy
	 * grandes). Internamente reutiliza el mismo recorrido que {@link #parseResult(String)}, pero
	 * cortando en el primer error en vez de acumularlos todos.
	 */
	/**
	 * @param content documento STXT completo a parsear.
	 * @return los nodos raíz del documento.
	 */
	public List<Node> parse(String content) {
		ParseResult result = doParse(content, true);
		if (result.hasErrors())
			throw result.getErrors().get(0);
		return result.getNodes();
	}

	/**
	 * Modo multi-error: parsea todo el contenido acumulando todos los errores encontrados (de
	 * sintaxis y de validación) sin abortar en el primero. Ver {@link ParseResult}.
	 */
	/**
	 * @param content documento STXT completo a parsear.
	 * @return el resultado acumulado, con los nodos raíz obtenidos y todos los errores encontrados.
	 */
	public ParseResult parseResult(String content) {
		return doParse(content, false);
	}

	/**
	 * Recorrido único compartido por {@link #parse(String)} y {@link #parseResult(String)}. Cuando
	 * {@code stopOnFirstError} es true, corta la lectura en cuanto aparece el primer error (no lee
	 * más líneas, no cierra el resto de nodos pendientes ni sigue validando), evitando recorrer un
	 * documento entero para un resultado que de todas formas se va a descartar.
	 */
	private ParseResult doParse(String content, boolean stopOnFirstError) {
		content = FileUtils.removeUTF8BOM(content);

		ParseResult result = new ParseResult();
		ArrayDeque<Node> stack = new ArrayDeque<>();
		List<Node> documents = new ArrayList<>();
		
		int lineNumber = 0;

		try (BufferedReader in = new BufferedReader(new StringReader(content))) {
			String line;
			while ((line = in.readLine()) != null) {
				lineNumber++;
				processLine(line, lineNumber, stack, documents, result, stopOnFirstError);
				if (stopOnFirstError && result.hasErrors())
					break;
			}
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}

		// Cerrar todos los nodos pendientes al EOF (nos la saltamos si ya hemos cortado antes)
		if (!(stopOnFirstError && result.hasErrors()))
			closeToLevel(stack, documents, 0, result, stopOnFirstError);

		// Agregamos nodos raíz al resultado
		for (Node doc : documents)
			result.addNode(doc);

		return result;
	}

	private void processLine(String line, int lineNumber, ArrayDeque<Node> stack, List<Node> documents, ParseResult result, boolean stopOnFirstError) {
		try {
			Node lastNode            = stack.isEmpty() ? null : stack.peek();
			int lastLevel           = lastNode != null ? lastNode.getLevel(): 0; 
			boolean lastNodeText    = lastNode != null && lastNode.isTextNode();

			// Parseamos línea
			LineIndent lineIndent = LineIndentParser.parseLine(line, lastNodeText, lastLevel, lineNumber);
			if (lineIndent == null)
				return;

			int currentLevel = lineIndent.indentLevel;

			// Si estamos dentro de un nodo texto, y el nivel indica que sigue siendo texto,
			// añadimos línea de texto y no creamos nodo.
			if (lastNodeText && currentLevel > lastLevel) {
				lastNode.addTextLine(lineIndent.lineWithoutIndent);
				return;
			}

			// Cerramos nodos hasta el nivel actual (esto "finaliza" y adjunta al padre/documentos)
			closeToLevel(stack, documents, currentLevel, result, stopOnFirstError);
			if (stopOnFirstError && result.hasErrors())
				return;

			// Creamos el nuevo nodo y lo dejamos "abierto" en la pila (NO lo adjuntamos aún)
			Node parent = stack.isEmpty() ? null : stack.peek();
			Node node = createNode(lineIndent, lineNumber, currentLevel, parent);
			
			// Pasamos a observers
			observeNode(node);

			// Añadimos a stack
			stack.push(node);
		} catch (ParseException e) {
			result.addError(e);
		} catch (RuntimeException e) {
			result.addError(new ParseException(lineNumber, "UNEXPECTED_ERROR", e.getMessage()));
		}
	}

	private void closeToLevel(ArrayDeque<Node> stack, List<Node> documents, int targetLevel, ParseResult result, boolean stopOnFirstError) {
		while (stack.size() > targetLevel) {
			Node completed = stack.pop();
			finishNode(completed, result);

			if (stack.isEmpty())	documents.add(completed);
			else					stack.peek().addChild(completed);

			if (stopOnFirstError && result.hasErrors())
				return;
		}
	}

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
	
	private void finishNode(Node node, ParseResult result) {
	    if (observers != null)
	        for (Observer o : observers)
	            o.onFinish(node);

	    if (validators != null)
	        for (Validator v : validators) {
	            try {
	                List<ValidationException> errors = v.validate(node);
	                if (errors != null)
	                    for (ValidationException e : errors)
	                        result.addError(e);
	            } catch (ValidationException e) {
	                result.addError(e);
	            } catch (RuntimeException e) {
	                result.addError(new ValidationException(node.getLine(), "VALIDATION_ERROR", e.getMessage()));
	            }
	        }
	}	
}
