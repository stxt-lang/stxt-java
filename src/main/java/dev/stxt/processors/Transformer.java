package dev.stxt.processors;

import dev.stxt.Node;

public interface Transformer extends Processor {
    /**
     * Transforma un nodo.
     * @return El nodo transformado, o null para eliminarlo
     */
    Node transform(Node node);
}