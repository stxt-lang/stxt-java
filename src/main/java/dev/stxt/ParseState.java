package dev.stxt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

class ParseState {
	// Pila de nodos abiertos según la indentación actual.
	private final ArrayDeque<Node> stack = new ArrayDeque<Node>();

	// Lista de documentos raíz resultantes del parseo.
	private final List<Node> documents = new ArrayList<>();

	public ParseState() {
	}

	public ArrayDeque<Node> getStack() {
		return stack;
	}

	public List<Node> getDocuments() {
		return documents;
	}

	public void addDocument(Node doc) {
		documents.add(doc);
	}
}
