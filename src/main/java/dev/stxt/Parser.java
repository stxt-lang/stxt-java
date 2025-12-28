package dev.stxt;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import dev.stxt.processors.Filter;
import dev.stxt.processors.Observer;
import dev.stxt.processors.Processor;
import dev.stxt.processors.Transformer;
import dev.stxt.processors.Validator;
import dev.stxt.utils.FileUtils;

public class Parser {
	
	/** 
	 *  Configuración de procesadores
	 */
	
	private List<Validator> validators;
	private List<Transformer> transformers;
	private List<Filter> filters;
	private List<Observer> observers;

	public void register(Processor p) {
	    if (p instanceof Validator) {
	        if (validators == null) {
	            validators = new ArrayList<Validator>();
	        }
	        validators.add((Validator) p);
	    }
	    if (p instanceof Transformer) {
	        if (transformers == null) {
	            transformers = new ArrayList<Transformer>();
	        }
	        transformers.add((Transformer) p);
	    }
	    if (p instanceof Filter) {
	        if (filters == null) {
	            filters = new ArrayList<Filter>();
	        }
	        filters.add((Filter) p);
	    }
	    if (p instanceof Observer) {
	        if (observers == null) {
	            observers = new ArrayList<Observer>();
	        }
	        observers.add((Observer) p);
	    }
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
		
		// Congelar documentos
		for (Node n: state.getDocuments())
			n.freeze();

		// Retorno documentos
		return state.getDocuments();
	}

	private void processLine(String line, int lineNumber, ParseState state) {
		LineIndent lineIndent = LineIndent.parseLine(line, lineNumber, state);
		if (lineIndent == null)
			return;

		int currentLevel = lineIndent.indentLevel;

		ArrayDeque<Node> stack = state.getStack();
		Node lastNode = !stack.isEmpty() ? stack.peek() : null;
		boolean lastNodeMultiline = lastNode != null && lastNode.isTextNode();

		// 1) Si estamos dentro de un nodo multilínea, y el nivel indica que sigue siendo texto,
		// añadimos línea de texto y no creamos nodo.
		if (lastNodeMultiline && currentLevel >= stack.size()) {
			lastNode.addTextLine(lineIndent.lineWithoutIndent);
			return;
		}

		// 2) No se permite saltar niveles de indentación por encima de la profundidad actual
		if (currentLevel > stack.size()) {
			throw new ParseException(lineNumber, "INDENTATION_LEVEL_NOT_VALID",
					"Level of indent incorrect: " + currentLevel);
		}

		// 3) Cerramos nodos hasta el nivel actual (esto "finaliza" y adjunta al padre/documentos)
		closeToLevel(state, currentLevel);

		// 4) Creamos el nuevo nodo y lo dejamos "abierto" en la pila (NO lo adjuntamos aún)
		Node parent = stack.isEmpty() ? null : stack.peek();
		Node node = createNode(lineIndent, lineNumber, currentLevel, parent);

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

			Node processed = finishNode(completed);

			if (processed == null) {
				// Eliminado por transformer (null) o por filter
				continue;
			}

			if (stack.isEmpty()) {
				// Es un documento raíz
				state.addDocument(processed);
			} else {
				// Se adjunta al padre que sigue en la pila
				stack.peek().getChildren().add(processed);
			}
		}
	}

	private Node createNode(LineIndent result, int lineNumber, int level, Node parent) {
		final String line = result.lineWithoutIndent;
		String name = null;
		String value = null;
		boolean multiline = false;

		int nodeIndex = line.indexOf(':');
		int textIndex = line.indexOf(">>");

		if ((nodeIndex != -1 && textIndex != -1) || (nodeIndex == -1 && textIndex == -1)) {
			throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);
		}

		// Inline value
		if (nodeIndex != -1) {
			name = line.substring(0, nodeIndex);
			value = line.substring(nodeIndex + 1).trim();
		}

		// Multiline Text
		if (textIndex != -1) {
			name = line.substring(0, textIndex);
			value = line.substring(textIndex + 2).trim();
			if (!value.isEmpty())
				throw new ParseException(lineNumber, "INLINE_VALUE_NOT_VALID", "Line not valid: " + line);
			multiline = true;
		}

		// Namespace por defecto: heredado del padre
		NameNamespace nn = NameNamespaceParser.parse(name, parent != null ? parent.getNamespace(): null, lineNumber, line);
		name = nn.getName();
		String namespace = nn.getNamespace();
		
		// Validamos nombre
		if (name.isEmpty())
			throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);

		// check namespace
		namespace = namespace.toLowerCase(Locale.ROOT);
		validateNamespaceFormat(namespace, lineNumber);

		return new Node(lineNumber, level, name, namespace, multiline, value);
	}

	/**
	 * Valida el namespace lógico.
	 *
	 * Reglas:
	 * - Solo minúsculas, dígitos y punto.
	 * - Puede empezar opcionalmente por '@'.
	 * - Debe ser una o varias etiquetas estilo dominio separadas por '.':
	 *   etiqueta := [a-z0-9]+
	 * ejemplos válidos: "xxx", "xxx.ddd", "zzz.ttt.ooo", "@xxx", "@xxx.ddd".
	 */
	private static final Pattern NAMESPACE_FORMAT = Pattern.compile("^@?[a-z0-9]+(\\.[a-z0-9]+)*$");
	private void validateNamespaceFormat(String namespace, int lineNumber) {
		if (namespace == null || namespace.isEmpty())
			return;

		if (!NAMESPACE_FORMAT.matcher(namespace).matches())
			throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Namespace not valid: " + namespace);
	}
	
	/**
	 * Aplica el pipeline a un nodo ya completo (con hijos añadidos):
	 * 1) Transformers en cadena (pueden devolver un nodo nuevo o null para eliminar)
	 * 2) Filters (si alguno no acepta -> null)
	 * 3) Validators (solo si el nodo sigue vivo)
	 */
	private Node finishNode(Node node) {
	    Node current = node;

	    // 1) Transformers
	    if (transformers != null) {
	        for (Transformer t : transformers) {
	            current = t.transform(current);
	            if (current == null) {
	                return null; // eliminado
	            }
	        }
	    }

	    // 2) Filters
	    if (filters != null) {
	        for (Filter f : filters) {
	            if (!f.accept(current)) {
	                return null; // filtrado
	            }
	        }
	    }

	    // 3) Observers (solo inspección)
	    if (observers != null) {
	        for (Observer o : observers) {
	            o.process(current);
	        }
	    }

	    // 4) Validators
	    if (validators != null) {
	        for (Validator v : validators) {
	            v.validate(current);
	        }
	    }

	    return current;
	}	
}
