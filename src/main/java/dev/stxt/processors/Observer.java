package dev.stxt.processors;

import dev.stxt.Node;

public interface Observer {
	void onCreate(Node node);
	void onFinish(Node node);
}
