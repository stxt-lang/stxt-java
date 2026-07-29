package dev.stxt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.utils.StringUtils;

/**
 * Nodo del árbol STXT. Mutable durante el parseo ({@link #addChild(Node)}/
 * {@link #addTextLine(String)} son públicos); una vez cerrado el documento se debe tratar como
 * de solo lectura. Representa tanto nodos INLINE (con {@link #getValue()}) como nodos de bloque
 * de texto BLOCK (con {@link #getTextLines()}), según {@link #isTextNode()}.
 */
public class Node {
	// STXT-SPEC 4.2: letras y dígitos Unicode (categorías L y Nd) más '-', '_' y espacio
	private static final Pattern VALID_NAME = Pattern.compile("^[\\p{L}\\p{Nd}\\-_ ]+$");

	private final String name;
	private final String normalizedName;
	private final String namespace;
	private final boolean textNode;

	private final String value;
	private List<String> textLines = new ArrayList<>();
	private final int line;
	private final int level;
	private List<Node> children = new ArrayList<>();

    /**
     * Crea un nodo sin namespace ni posición conocida en el documento (línea/nivel = -1).
     * Pensado para construir nodos fuera del parseo normal (p. ej. en tests).
     *
     * @param name nombre del nodo.
     * @param textNode {@code true} si es un nodo de bloque de texto (BLOCK); {@code false} si es INLINE.
     * @param value valor inline del nodo (nodo INLINE), ignorado si es BLOCK.
     */
    public Node(String name, boolean textNode, String value) {
        this(-1,-1,name,null,textNode,value);
    }
    
	/**
	 * Crea un nodo con namespace explícito pero sin posición conocida en el documento
	 * (línea/nivel = -1). Pensado para construir nodos fuera del parseo normal (p. ej. en tests).
	 *
	 * @param name nombre del nodo.
	 * @param namespace namespace del nodo, o {@code null} si no tiene.
	 * @param textNode {@code true} si es un nodo de bloque de texto (BLOCK); {@code false} si es INLINE.
	 * @param value valor inline del nodo (nodo INLINE), ignorado si es BLOCK.
	 */
	public Node(String name, String namespace, boolean textNode, String value) {
	    this(-1,-1,name,namespace,textNode,value);
	}
	
	/**
	 * Crea un nodo con su posición completa en el documento. Es el constructor que usa el
	 * {@link Parser} al parsear.
	 *
	 * @param line número de línea del documento donde se abre el nodo.
	 * @param level nivel de indentación del nodo.
	 * @param name nombre del nodo.
	 * @param namespace namespace del nodo, o {@code null} si no tiene.
	 * @param textNode {@code true} si es un nodo de bloque de texto (BLOCK); {@code false} si es INLINE.
	 * @param value valor inline del nodo (nodo INLINE), ignorado si es BLOCK.
	 * @throws ParseException si el nombre o el namespace no son válidos.
	 */
	public Node(int line, int level, String name, String namespace, boolean textNode, String value) {
		this.level = level;
		this.line = line;
		this.name = StringUtils.compactSpaces(name);
		this.normalizedName = StringUtils.normalize(name);
		this.namespace = StringUtils.lowerCase(namespace);
		this.value = (value == null ? "" : value.trim());
		this.textNode = textNode;
		NamespaceValidator.validateNamespaceFormat(this.namespace, line);

		if (!this.value.isEmpty() && this.isTextNode())
			throw new IllegalArgumentException("Not empty value with textNode");

		if (name == null || !VALID_NAME.matcher(name).matches()) {
		    throw new ParseException(line, "INVALID_NODE_NAME", "Node name contains invalid characters: " + name);
		}

		if (this.normalizedName.isEmpty()) {
		    throw new ParseException(line, "INVALID_NODE_NAME", "Node name not valid: " + name);
		}
	}

	/** @param line línea de texto a añadir a un nodo BLOCK ({@link #isTextNode()}). */
	public void addTextLine(String line) {
		this.textLines.add(line);
	}

	/** @return nombre original del nodo tal como aparece en el documento (con espacios compactados). */
	public String getName() {
		return name;
	}

	/** @return nombre canónico del nodo, usado para comparar/buscar por identidad estructural. */
	public String getNormalizedName() {
		return normalizedName;
	}

	/** @return nombre canónico prefijado por namespace ({@code namespace:nombre}), o solo el nombre si no hay namespace. */
	public String getQualifiedName() {
		return namespace.isEmpty() ? normalizedName : namespace + ":" + normalizedName;
	}

	/** @return namespace efectivo del nodo (propio o heredado del padre), en minúsculas, o cadena vacía si no tiene. */
	public String getNamespace() {
		return namespace;
	}

	/** @return hijos del nodo en orden de aparición, vista de solo lectura. */
	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/** @param node hijo ya cerrado a añadir al final de la lista de hijos de este nodo. */
	public void addChild(Node node) {
		children.add(node);
	}

	/** @return valor inline del nodo (nodo INLINE), o cadena vacía si es un nodo BLOCK. */
	public String getValue() {
		return value;
	}

	/** @return líneas de texto de un nodo BLOCK ({@link #isTextNode()}), en orden de aparición. */
	public List<String> getTextLines() {
		return textLines;
	}

	/** @return número de línea del documento donde se abrió este nodo. */
	public int getLine() {
		return line;
	}

	/** @return nivel de indentación del nodo (0 para nodos raíz). */
	public int getLevel() {
		return level;
	}

	/** @return {@code true} si el nodo es de bloque de texto (BLOCK, {@code >>}); {@code false} si es INLINE. */
	public boolean isTextNode() {
		return textNode;
	}

	/** @return contenido textual del nodo: las líneas de texto unidas con '\n' si es BLOCK, o el valor inline si no. */
	public String getText() {
		if (isTextNode())
			return String.join("\n", textLines);
		else
			return value;
	}

	/**
	 * Busca el único hijo directo con ese nombre en el namespace propio de este nodo.
	 *
	 * @param cname nombre del hijo buscado.
	 * @return el hijo encontrado, o {@code null} si no hay ninguno.
	 * @throws STXTException con código {@code AMBIGUOUS_CHILD} si hay más de un hijo que encaja;
	 *         usar {@link #getChildren(String)} en ese caso.
	 */
	public Node getChild(String cname) {
		return getChild(cname, this.namespace);
	}

	/**
	 * Busca el único hijo directo con ese nombre en el namespace indicado.
	 *
	 * @param cname nombre del hijo buscado.
	 * @param namespace namespace en el que buscar.
	 * @return el hijo encontrado, o {@code null} si no hay ninguno.
	 * @throws STXTException con código {@code AMBIGUOUS_CHILD} si hay más de un hijo que encaja;
	 *         usar {@link #getChildren(String, String)} en ese caso.
	 */
	public Node getChild(String cname, String namespace) {
		List<Node> result = getChildren(cname, namespace);
		if (result.size() > 1)
			throw new STXTException("AMBIGUOUS_CHILD", "More than 1 child. Use getChildren");
		if (result.size() == 0)
			return null;
		return result.get(0);
	}

	// Fast access methods to children
	/**
	 * @param cname nombre del hijo buscado.
	 * @return todos los hijos directos con ese nombre en el namespace propio de este nodo.
	 */
	public List<Node> getChildren(String cname) {
		return getChildren(cname, this.namespace);
	}

	/**
	 * @param cname nombre del hijo buscado.
	 * @param namespace namespace en el que buscar.
	 * @return todos los hijos directos con ese nombre en el namespace indicado.
	 */
	public List<Node> getChildren(String cname, String namespace) {
		String key = StringUtils.normalize(cname);
		List<Node> result = new ArrayList<Node>();

		for (Node child : children) {
			if (child.getNormalizedName().equals(key) && Objects.equals(child.getNamespace(), namespace))
				result.add(child);
		}

		return result;
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Node{");
	    sb.append("line=").append(line);
	    sb.append(", level=").append(level);
	    sb.append(", name='").append(name).append('\'');
	    if (!namespace.isEmpty()) sb.append(", ns='").append(namespace).append('\'');
	    sb.append(", text=").append(textNode);
	    if (!textNode && !value.isEmpty()) sb.append(", value='").append(value).append('\'');
	    if (textNode) sb.append(", lines=").append(textLines.size());
	    sb.append(", children=").append(children.size());
	    sb.append('}');
	    return sb.toString();
	}
	
}