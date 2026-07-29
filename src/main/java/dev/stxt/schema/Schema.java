package dev.stxt.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.stxt.NamespaceValidator;
import dev.stxt.exceptions.SchemaException;
import dev.stxt.utils.StringUtils;

/** Schema de un namespace: conjunto de {@link NodeDefinition} válidas para los nodos de ese namespace. */
public class Schema {
	/** Namespace del propio lenguaje de schemas, {@code @stxt.schema}. */
	public static final String SCHEMA_NAMESPACE = "@stxt.schema";

	private Map<String, NodeDefinition> nodes = new LinkedHashMap<String, NodeDefinition>();
	private final String namespace;
	
	/**
	 * @param namespace namespace al que aplica este schema.
	 * @param line número de línea, para el mensaje de error.
	 */
	public Schema(String namespace, int line) {
		this.namespace = StringUtils.lowerCase(namespace);
		NamespaceValidator.validateNamespaceFormat(this.namespace, line);
	}
	
	/** @return definiciones de nodo, indexadas por su nombre canónico. */
	public Map<String, NodeDefinition> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}
	
	/**
	 * @param name nombre del nodo buscado.
	 * @return la definición del nodo con ese nombre, o {@code null} si no está definido en este schema.
	 */
	public NodeDefinition getNodeDefinition(String name) {
		return nodes.get(StringUtils.normalize(name));
	}
	
	/**
	 * @param nodeDefinition definición de nodo a añadir.
	 * @throws dev.stxt.exceptions.SchemaException si ya había una definición de nodo con el mismo nombre.
	 */
	public void addNodeDefinition(NodeDefinition nodeDefinition) {
		String qname = nodeDefinition.getNormalizedName();
		if (nodes.containsKey(qname)) throw new SchemaException("NODE_DEF_ALREADY_DEFINED", "Exists a previous node definition with: " + qname);
		nodes.put(qname, nodeDefinition);
	}
	
	/** @return namespace al que aplica este schema. */
	public String getNamespace() {
		return namespace;
	}
}
