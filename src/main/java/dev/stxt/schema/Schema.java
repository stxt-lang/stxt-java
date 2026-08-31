package dev.stxt.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.stxt.NamespaceValidator;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.utils.StringUtils;

/** Schema of a namespace: the set of {@link NodeDefinition} valid for the nodes of that namespace. */
public class Schema {
	/** Namespace of the schema language itself, {@code @stxt.schema}. */
	public static final String SCHEMA_NAMESPACE = "@stxt.schema";

	/** Namespace of the template language, {@code @stxt.template}. */
	public static final String TEMPLATE_NAMESPACE = "@stxt.template";

	private Map<String, NodeDefinition> nodes = new LinkedHashMap<String, NodeDefinition>();
	private final String namespace;
	private final String description;
	
	/**
	 * Creates an empty schema for a namespace, with no description.
	 *
	 * @param namespace namespace this schema applies to.
	 * @param line line number, for the error message.
	 */
	public Schema(String namespace, int line) {
		this(namespace, line, null);
	}

	/**
	 * Creates an empty schema for a namespace.
	 *
	 * @param namespace namespace this schema applies to.
	 * @param line line number, for the error message.
	 * @param description optional description of the schema (STXT-SCHEMA-SPEC 6.1), or {@code null}.
	 */
	public Schema(String namespace, int line, String description) {
		this.namespace = StringUtils.lowerCase(namespace);
		this.description = description;
		NamespaceValidator.validateNamespaceFormat(this.namespace, line);
	}

	/** {@return the description of the schema, or {@code null} if it has none} */
	public String getDescription() {
		return description;
	}
	
	/** {@return the node definitions, indexed by their canonical name} */
	public Map<String, NodeDefinition> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}
	
	/**
	 * Looks up the definition of a node by name.
	 *
	 * @param name name of the node to look for.
	 * @return the definition of the node with that name, or {@code null} if it is not defined in this schema.
	 */
	public NodeDefinition getNodeDefinition(String name) {
		return nodes.get(StringUtils.normalize(name));
	}
	
	/**
	 * Adds the definition of a node to this schema.
	 *
	 * @param nodeDefinition node definition to add.
	 * @throws ValidationException with code {@code NODE_DUPLICATED} if there already was a node definition with the same name.
	 */
	public void addNodeDefinition(NodeDefinition nodeDefinition) {
		String canonicalName = nodeDefinition.getCanonicalName();
		if (nodes.containsKey(canonicalName))
			throw new ValidationException(ParseException.NO_LINE, "NODE_DUPLICATED", "A node definition with the same name already exists: " + canonicalName);
		nodes.put(canonicalName, nodeDefinition);
	}
	
	/** {@return the namespace this schema applies to} */
	public String getNamespace() {
		return namespace;
	}
}
