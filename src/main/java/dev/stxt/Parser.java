package dev.stxt;

import dev.stxt.utils.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.stxt.exceptions.LimitException;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTIOException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.processors.Observer;
import dev.stxt.processors.StreamObserver;
import dev.stxt.processors.Validator;
import dev.stxt.utils.FileUtils;

/**
 * Line-by-line STXT parsing engine. It knows nothing about schemas: semantic validation is
 * plugged in through {@link #registerValidator(Validator)}, process observation through
 * {@link #registerObserver(Observer)} and result observation through
 * {@link #registerStreamObserver(StreamObserver)}. See {@link dev.stxt.runtime.STXT} for the
 * usual entry point, already wired up with validation.
 *
 * <p>Three entry points share one traversal: {@link #parse(String)} (fail-fast),
 * {@link #parseResult(String)} (multi-error) and {@link #parseStream(Reader)} (lines in,
 * nothing retained). Which callbacks fire never depends on the entry point, only on what is
 * registered.
 *
 * <p>The parser aborts on inputs that exceed its limits (STXT-SPEC 11.2), set to the
 * {@code DEFAULT_MAX_*} values of {@link Constants} unless configured through the
 * {@code setMax*} methods. A limit error is a {@link LimitException} and is in every case the
 * last one emitted: the nodes still open are not closed nor notified.
 */
public class Parser {
	private List<Validator> validators;
	private List<Observer> observers;
	private List<StreamObserver> streamObservers;

	private int maxNesting    = Constants.DEFAULT_MAX_NESTING;
	private int maxLineLength = Constants.DEFAULT_MAX_LINE_LENGTH;
	private int maxInputSize  = Constants.DEFAULT_MAX_INPUT_SIZE;

	/** Creates a parser with no validator and no observer registered, and the default limits. */
	public Parser() {
	}

	/**
	 * Sets the maximum open nesting levels (STXT-SPEC 11.2); level 0 is the first.
	 *
	 * @param maxNesting the limit, or -1 to disable it. Default {@link Constants#DEFAULT_MAX_NESTING}.
	 */
	public void setMaxNesting(int maxNesting) {
		this.maxNesting = maxNesting;
	}

	/**
	 * Sets the maximum length of one input line, indentation included (STXT-SPEC 11.2).
	 *
	 * @param maxLineLength the limit, or -1 to disable it. Default {@link Constants#DEFAULT_MAX_LINE_LENGTH}.
	 */
	public void setMaxLineLength(int maxLineLength) {
		this.maxLineLength = maxLineLength;
	}

	/**
	 * Sets the maximum total input consumed (STXT-SPEC 11.2).
	 *
	 * @param maxInputSize the limit, or -1 to disable it. Default {@link Constants#DEFAULT_MAX_INPUT_SIZE}.
	 */
	public void setMaxInputSize(int maxInputSize) {
		this.maxInputSize = maxInputSize;
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
	 * Registers a stream observer, notified with each completed root node and each error, in
	 * every mode.
	 *
	 * @param so the {@link StreamObserver} to register.
	 */
	public void registerStreamObserver(StreamObserver so) {
        if (streamObservers == null) {
            streamObservers = new ArrayList<>();
        }
        streamObservers.add(so);
	}

	/**
	 * Same as {@link #parse(String)} but reading the content from a file. The file is read line
	 * by line (streaming), so the size and line-length limits (STXT-SPEC 11.2) apply
	 * incrementally and a pathologically large file aborts with {@code LIMIT_INPUT_SIZE_EXCEEDED}
	 * or {@code LIMIT_LINE_LENGTH_EXCEEDED} at bounded memory instead of being loaded whole.
	 *
	 * @param srcFile file holding the STXT document.
	 * @return the root nodes of the parsed document.
	 * @throws STXTIOException if the file cannot be read.
	 */
	public List<Node> parseFile(File srcFile) {
		try (Reader reader = FileUtils.newFileReader(srcFile)) {
			ParseResult result = new ParseResult();
			parseReaderLines(reader, result, true);
			if (result.hasErrors())
				throw result.getErrors().get(0);
			return result.getNodes();
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}
	}

	/**
	 * Same as {@link #parseResult(String)} but reading the content from a file. The file is read
	 * line by line (streaming), so the size and line-length limits (STXT-SPEC 11.2) apply
	 * incrementally, aborting a pathologically large file at bounded memory.
	 *
	 * @param srcFile file holding the STXT document.
	 * @return the result of parsing in multi-error mode.
	 * @throws STXTIOException if the file cannot be read.
	 */
	public ParseResult parseResultFile(File srcFile) {
		try (Reader reader = FileUtils.newFileReader(srcFile)) {
			ParseResult result = new ParseResult();
			parseReaderLines(reader, result, false);
			return result;
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
	 * validation) without bailing out on the first one — except a {@link LimitException}, which
	 * aborts and is in every case the last error collected. See {@link ParseResult}.
	 *
	 * @param content the whole STXT document to parse.
	 * @return the collected result, with the root nodes obtained and every error found.
	 */
	public ParseResult parseResult(String content) {
		return doParse(content, false);
	}

	/**
	 * Streaming mode: input read line by line from a reader, and nothing retained: no nodes, no
	 * errors. Results reach the program only through the registered {@link StreamObserver}s
	 * (each completed root by {@code onRootNode()}, each error by {@code onError()}), so memory
	 * holds one root tree at a time. This is the entry point for files that do not fit in
	 * memory. The reader is not closed.
	 *
	 * @param reader the input; buffered internally if it is not a {@link BufferedReader}.
	 * @throws STXTIOException if the reader cannot be read.
	 */
	public void parseStream(Reader reader) {
		parseReaderLines(reader, null, false);
	}

	/**
	 * Shared reader traversal for the file and {@link Reader} entry points. It feeds
	 * {@link #parseLines} a {@link LimitedLineReader}, which reads the input incrementally and
	 * cuts a line as soon as it would exceed {@code maxLineLength} or {@code maxInputSize}, so
	 * the limits (STXT-SPEC 11.2) abort at bounded memory even on a single line with no line
	 * break. When both limits are disabled ({@code -1}) it reads whole lines, as the caller has
	 * opted out of protection.
	 */
	private void parseReaderLines(Reader reader, ParseResult result, boolean stopOnFirstError) {
		try {
			parseLines(new LimitedLineReader(reader, maxLineLength, maxInputSize), result, stopOnFirstError);
		} catch (UncheckedIOException e) {
			throw new STXTIOException(e.getCause());
		}
	}

	/**
	 * A line iterator over a {@link Reader} that enforces the parser's size limits as it reads,
	 * never buffering much more than one line's limit. It splits lines like {@link BufferedReader}
	 * ({@code \n}, {@code \r} and {@code \r\n} all terminate a line and are stripped) so the
	 * observable result matches the old whole-string path for normal input, but it force-cuts a
	 * line once its length would exceed {@code maxLineLength}, or once the total consumed would
	 * exceed {@code maxInputSize}, and returns the partial line. {@link #parseLines} then sees a
	 * line longer than the limit (or a running total over the limit) and aborts with the matching
	 * {@code LIMIT_*} code before the rest of the input is ever read. With both limits disabled
	 * ({@code -1}) it never force-cuts and returns whole lines.
	 */
	private static final class LimitedLineReader implements Iterator<String> {
		private final Reader in;
		private final int maxLineLength;	// -1 disables the per-line cut
		private final int maxInputSize;		// -1 disables the total-size cut
		private String pending;				// the next line, once read; null until read or at EOF
		private boolean eof;
		private long emitted;				// chars plus one separator per line already returned
		private int pushback = -1;			// a lookahead char kept for the \r\n split

		LimitedLineReader(Reader reader, int maxLineLength, int maxInputSize) {
			this.in = reader instanceof BufferedReader ? reader : new BufferedReader(reader);
			this.maxLineLength = maxLineLength;
			this.maxInputSize = maxInputSize;
		}

		@Override
		public boolean hasNext() {
			if (pending == null && !eof)
				pending = readLine();
			return pending != null;
		}

		@Override
		public String next() {
			if (!hasNext())
				throw new java.util.NoSuchElementException();
			String line = pending;
			pending = null;
			// Mirror parseLines' own accounting (line length plus one for the separator), so the
			// total-size cut lines up with the LIMIT_INPUT_SIZE_EXCEEDED check there.
			emitted += line.length() + 1L;
			return line;
		}

		private String readLine() {
			StringBuilder sb = new StringBuilder();
			while (true) {
				int c = read();
				if (c == -1) {
					eof = true;
					return sb.length() == 0 ? null : sb.toString();
				}
				if (c == '\n')
					return sb.toString();
				if (c == '\r') {
					int next = read();		// swallow the \n of a \r\n; keep any other char
					if (next != '\n' && next != -1)
						pushback = next;
					return sb.toString();
				}
				sb.append((char) c);
				// Force-cut so the buffered line never grows past its limit; the returned partial
				// line is over the limit, so parseLines aborts and never asks for another line.
				if (maxLineLength != -1 && sb.length() > maxLineLength)
					return sb.toString();
				if (maxInputSize != -1 && emitted + sb.length() > maxInputSize)
					return sb.toString();
			}
		}

		private int read() {
			try {
				if (pushback != -1) {
					int c = pushback;
					pushback = -1;
					return c;
				}
				return in.read();
			} catch (java.io.IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	/**
	 * Same as {@link #parseStream(Reader)} but taking the input as lines (each item one line,
	 * without its line break).
	 *
	 * @param lines the input, line by line.
	 */
	public void parseStream(Iterable<String> lines) {
		parseLines(lines.iterator(), null, false);
	}

	/**
	 * Single traversal shared by {@link #parse(String)} and {@link #parseResult(String)}. When
	 * {@code stopOnFirstError} is true, reading stops as soon as the first error shows up (no more
	 * lines are read, no pending nodes are closed and no further validation happens), avoiding a
	 * full walk over a document whose result is going to be discarded anyway.
	 */
	private ParseResult doParse(String content, boolean stopOnFirstError) {
		ParseResult result = new ParseResult();

		try (BufferedReader in = new BufferedReader(new StringReader(content))) {
			parseLines(in.lines().iterator(), result, stopOnFirstError);
		} catch (UncheckedIOException e) {
			throw new STXTIOException(e.getCause());
		} catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}

		return result;
	}

	/**
	 * Shared traversal. With a result, roots and errors are collected into it (parse and
	 * parseResult); with null, nothing is retained (parseStream). Either way every registered
	 * callback fires the same.
	 */
	private void parseLines(Iterator<String> lines, ParseResult result, boolean stopOnFirstError) {
		ArrayDeque<Node> stack = new ArrayDeque<>();
		int lineNumber = 0;
		long consumed = 0;

		while (lines.hasNext()) {
			String line = lines.next();
			lineNumber++;

			// A UTF-8 BOM only means anything at the very start of the input (spec 3)
			if (lineNumber == 1)
				line = FileUtils.removeUTF8BOM(line);

			// Limits first (spec 11.2): a limit error aborts, leaving the open nodes
			// unclosed and unnotified.
			if (maxLineLength != -1 && line.length() > maxLineLength) {
				emitError(new LimitException(lineNumber, "LIMIT_LINE_LENGTH_EXCEEDED",
						"Line longer than " + maxLineLength + " characters"), result);
				return;
			}

			consumed += line.length() + 1;	// the line separator counts as one
			if (maxInputSize != -1 && consumed > maxInputSize) {
				emitError(new LimitException(lineNumber, "LIMIT_INPUT_SIZE_EXCEEDED",
						"Input larger than " + maxInputSize + " characters"), result);
				return;
			}

			try {
				processLine(line, lineNumber, stack, result, stopOnFirstError);
			} catch (LimitException e) {
				emitError(e, result);
				return;
			}

			if (stopOnFirstError && result.hasErrors())
				return;
		}

		// Close every pending node at EOF
		closeToLevel(stack, 0, result, stopOnFirstError);
	}

	private void processLine(String line, int lineNumber, ArrayDeque<Node> stack, ParseResult result, boolean stopOnFirstError) {
		try {
			Node lastNode            = stack.isEmpty() ? null : stack.peek();
			// The stack holds the open nodes, one per level: its size is the level of the next line's parent
			// With no open node the reference level is -1 (spec 8.3): the first line of the
			// document, and the first after every node has been closed, must be at level 0.
			int lastLevel           = lastNode != null ? stack.size() - 1 : -1;
			boolean lastNodeText    = lastNode instanceof TextNode;

			// Parse the line
			LineIndent lineIndent = LineIndentParser.parseLine(line, lastNodeText, lastLevel, lineNumber);

			if (lineIndent.isComment) {
				// Its indentation was validated by parseLine like a node's (spec 9), but it never
				// becomes the reference level: the stack (and so lastLevel) is only moved by nodes.
				// A comment at the level of an open block node (or shallower) closes the block
				// (spec 6.1 and 9.1): a block is a literal and cannot be commented from inside.
				// Only the block closes; the comment does not touch the rest of the hierarchy.
				if (lastNodeText)
					closeToLevel(stack, stack.size() - 1, result, stopOnFirstError);

				// Hand it over to the observers
				if (observers != null)
					for (Observer o : observers)
						o.onComment(lineNumber, line);
				return;
			}

			int currentLevel = lineIndent.indentLevel;

			// When we are inside a text node and the level says it is still text,
			// append the text line instead of creating a node.
			if (lineIndent.isBlock) {
				TextNode textNode = (TextNode) lastNode;
				textNode.addTextLine(lineIndent.lineWithoutIndent);

				// Notify the observers about the text line
				if (observers != null)
					for (Observer o : observers)
						o.onTextLine(textNode, lineNumber, line, lineIndent);
				return;
			}

			// Empty lines are ignored
			if (lineIndent.isEmpty())
				return;

			// Nesting limit (spec 11.2): only a node line can open a new level. Comment and
			// block text lines returned above; with the consecutive-level rule this triggers
			// exactly when the first node at level maxNesting opens.
			if (maxNesting != -1 && currentLevel >= maxNesting)
				throw new LimitException(lineNumber, "LIMIT_NESTING_EXCEEDED",
						"Nesting deeper than " + maxNesting + " levels");

			// Close nodes down to the current level (this "finishes" them: observers and validators run)
			closeToLevel(stack, currentLevel, result, stopOnFirstError);
			if (stopOnFirstError && result.hasErrors())
				return;

			// Create the new node, attach it to its parent (or keep it as a root if the stack is
			// empty) and leave it "open" on the stack. Attaching links both ends: the node already
			// knows its parent, and so its effective namespace and its level, when the observers
			// see it.
			Node parent = stack.isEmpty() ? null : stack.peek();
			Node node = createNode(lineIndent, lineNumber);
			if (parent != null)
				((InlineNode) parent).addChild(node);

			// Hand it over to the observers
			observeNode(node, line);

			// Push it onto the stack
			stack.push(node);
		} catch (LimitException e) {
			throw e;
		} catch (ParseException e) {
			emitError(e, result);
		} catch (RuntimeException e) {
			emitError(new ParseException(lineNumber, "UNEXPECTED_ERROR", e.getMessage()), result);
		}
	}

	private void closeToLevel(ArrayDeque<Node> stack, int targetLevel, ParseResult result, boolean stopOnFirstError) {
		while (stack.size() > targetLevel) {
			Node completed = stack.pop();

			// A closing block node drops its final empty lines (STXT-SPEC §10.3): they are not
			// content, only visual separation or an editor's final line breaks. The validators
			// and observers already see the trimmed node; onTextLine did fire for these lines
			// while the block was open, as process observation of the source.
			if (completed instanceof TextNode text)
				text.removeTrailingEmptyLines();

			finishNode(completed, result);

			// A closed root: the stream observers receive it, the result collects it
			if (stack.isEmpty()) {
				if (streamObservers != null)
					for (StreamObserver so : streamObservers)
						so.onRootNode(completed);

				if (result != null)
					result.addNode(completed);
			}

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

		if (textNode && !StringUtils.trim(value).isEmpty())
				throw new ParseException(lineNumber, "BLOCK_VALUE_NOT_ALLOWED", "Line not valid: " + line);

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

	private Node observeNode(Node node, String line) {
	    if (observers != null)
	        for (Observer o: observers)
	            o.onCreate(node, line);

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
	                        emitError(e, result);
	            } catch (ValidationException e) {
	                emitError(e, result);
	            } catch (RuntimeException e) {
	                emitError(new ValidationException(node.getLine(), "UNEXPECTED_ERROR", e.getMessage()), result);
	            }
	        }
	}

	/**
	 * Every error goes through here: collected into the result when there is one, and notified
	 * to the stream observers always, in order of appearance.
	 */
	private void emitError(ParseException error, ParseResult result) {
		if (result != null)
			result.addError(error);

		if (streamObservers != null)
			for (StreamObserver so : streamObservers)
				so.onError(error);
	}
}
