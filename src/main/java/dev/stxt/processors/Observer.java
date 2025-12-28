package dev.stxt.processors;

import dev.stxt.Node;

public interface Observer extends Processor {
	void onCreate(Node node);
	void onFinish(Node node);
}
