package dev.stxt.processors;

import dev.stxt.Node;

/** Hook de proceso notificado por el {@link dev.stxt.Parser} al abrir y cerrar cada nodo. */
public interface Observer {
	/** @param node nodo recién abierto (aún sin hijos ni valor completos). */
	void onCreate(Node node);
	/** @param node nodo recién cerrado, ya con todos sus hijos y su valor completos. */
	void onFinish(Node node);
}
