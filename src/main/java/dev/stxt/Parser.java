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
 * Line-by-line STXT parsing engine. It knows nothing about schemas: semantic validation is
 * plugged in through {@link #registerValidator(Validator)} and {@link #registerObserver(Observer)}.
 * See {@link dev.stxt.runtime.STXT} for the usual entry point, already wired up with validation.
 */
public class Parser {
	private List<Validator> validators;
	private List<Observer> observers;

	/** Creates a parser with no validator and no observer registered. */
	public Parser() {
	}

	/**
	 * Registers a validator, invoked when each node is closed.
	 *
	 * @param v the {@link Validator} to register, invoked when each node is closed during parsing.
	 */
	public void registerValidator(Validator v) {
        if (validators == null) {
            validators = new ArrayList<>();
        }
        validators.add(v);
	}
	/**
	 * Registers an observer, notified when each node is opened and closed.
	 *
	 * @param o the {@link Observer} to register, notified when each node is opened and closed during parsing.
	 */
	public void registerObserver(Observer o) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(o);
	}

	/**
	 * Same as {@link #parse(String)} but reading the content from a file.
	 *
	 * @param srcFile file holding the STXT document.
	 * @return the root nodes of the parsed document.
	 * @throws STXTIOException if the file cannot be read.
	 */
	public List<Node> parseFile(File srcFile) {
		try {
			return parse(FileUtils.readFileContent(srcFile));
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}
	}

	/**
	 * Same as {@link #parseResult(String)} but reading the content from a file.
	 *
	 * @param srcFile file holding the STXT document.
	 * @return the result of parsing in multi-error mode.
	 * @throws STXTIOException if the file cannot be read.
	 */
	public ParseResult parseResultFile(File srcFile) {
		try {
			return parseResult(FileUtils.readFileContent(srcFile));
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}
	}

	/**
	 * Traditional fail-fast mode: throws the first error found (either syntax or validation),
	 * without walking the rest of the document (which matters on very large documents).
	 * Internally it reuses the same traversal as {@link #parseResult(String)}, but stopping at
	 * the first error instead of collecting them all.
	 *
	 * @param content the whole STXT document to parse.
	 * @return the root nodes of the document.
	 */
	public List<Node> parse(String content) {
		ParseResult result = doParse(content, true);
		if (result.hasErrors())
			throw result.getErrors().get(0);
		return result.getNodes();
	}

	/**
	 * Multi-error mode: parses the whole content collecting every error found (both syntax and
	 * validation) without bailing out on the first one. See {@link ParseResult}.
	 *
	 * @param content the whole STXT document to parse.
	 * @return the collected result, with the root nodes obtained and every error found.
	 */
	public ParseResult parseResult(String content) {
		return doParse(content, false);
	}

	/**
	 * Single traversal shared by {@link #parse(String)} and {@link #parseResult(String)}. When
	 * {@code stopOnFirstError} is true, reading stops as soon as the first error shows up (no more
	 * lines are read, no pending nodes are closed and no further validation happens), avoiding a
	 * full walk over a document whose result is going to be discarded anyway.
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

		// Close every pending node at EOF (skipped if we already bailed out earlier)
		if (!(stopOnFirstError && result.hasErrors()))
			closeToLevel(stack, 0, result, stopOnFirstError);

		// Add the root nodes to the result
		for (Node doc : documents)
			result.addNode(doc);

		return result;
	}

	private void processLine(String line, int lineNumber, ArrayDeque<Node> stack, List<Node> documents, ParseResult result, boolean stopOnFirstError) {
		try {
			Node lastNode            = stack.isEmpty() ? null : stack.peek();
			// The stack holds the open nodes, one per level: its size is the level of the next line's parent
			int lastLevel           = lastNode != null ? stack.size() - 1 : 0;
			boolean lastNodeText    = lastNode instanceof TextNode;

			// Parse the line
			LineIndent lineIndent = LineIndentParser.parseLine(line, lastNodeText, lastLevel, lineNumber);
			if (lineIndent == null)
				return;

			int currentLevel = lineIndent.indentLevel;

			// If we are inside a text node, and the level says it is still text,
			// append a text line instead of creating a node.
			if (lastNodeText && currentLevel > lastLevel) {
				((TextNode) lastNode).addTextLine(lineIndent.lineWithoutIndent);
				return;
			}

			// Close nodes down to the current level (this "finishes" them: observers and validators run)
			closeToLevel(stack, currentLevel, result, stopOnFirstError);
			if (stopOnFirstError && result.hasErrors())
				return;

			// Create the new node, attach it to its parent (or to the documents if it is a root)
			// and leave it "open" on the stack. Attaching links both ends: the node already knows
			// its parent, and so its effective namespace and its level, when the observers see it.
			Node parent = stack.isEmpty() ? null : stack.peek();
			Node node = createNode(lineIndent, lineNumber);
			if (parent == null)		documents.add(node);
			else					((InlineNode) parent).addChild(node);

			// Hand it over to the observers
			observeNode(node);

			// Push it onto the stack
			stack.push(node);
		} catch (ParseException e) {
			result.addError(e);
		} catch (RuntimeException e) {
			result.addError(new ParseException(lineNumber, "UNEXPECTED_ERROR", e.getMessage()));
		}
	}

	private void closeToLevel(ArrayDeque<Node> stack, int targetLevel, ParseResult result, boolean stopOnFirstError) {
		while (stack.size() > targetLevel) {
			Node completed = stack.pop();
			finishNode(completed, result);

			if (stopOnFirstError && result.hasErrors())
				return;
		}
	}

	private Node createNode(LineIndent lineIndent, int lineNumber) {
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

		// The namespace the line declares, if any; inheritance from the parent is resolved by
		// the node itself through its parent link (Node.getNamespace())
		NameNamespace nn = NameNamespaceParser.parse(name, null, lineNumber, line);
		name = nn.getName();
		String namespace = nn.getNamespace();
		
		// Validate the name
		if (name.isEmpty())
			throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + line);

		// Create the node
		if (textNode)	return new TextNode(name, namespace, null, lineNumber);
		else			return new InlineNode(name, namespace, value, lineNumber);
	}

	// -------------------------------------------
	// Validation, transformation, etc. methods
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
