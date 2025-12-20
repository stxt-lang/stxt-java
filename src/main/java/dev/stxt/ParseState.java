package dev.stxt;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class ParseState {
	// Pila de nodos abiertos según la indentación actual.
	private final Stack<Node> stack = new Stack<>();

	// Lista de documentos raíz resultantes del parseo.
	private final List<Node> documents = new ArrayList<>();

	public ParseState() {
	}

	public Stack<Node> getStack() {
		return stack;
	}

	public boolean stackIsEmpty() {
		return stack.isEmpty();
	}

	public Node stackPeek() {
		return stack.peek();
	}

	public void stackPush(Node node) {
		stack.push(node);
	}

	public Node stackPop() {
		return stack.pop();
	}

	public List<Node> getDocuments() {
		return documents;
	}

	public void addDocument(Node doc) {
		documents.add(doc);
	}
}
