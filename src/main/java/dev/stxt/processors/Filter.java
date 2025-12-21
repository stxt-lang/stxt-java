package dev.stxt.processors;

import dev.stxt.Node;

public interface Filter extends Processor {
    boolean accept(Node node);
}