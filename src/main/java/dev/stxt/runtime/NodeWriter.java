package dev.stxt.runtime;

import java.util.List;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;

/** Serializes a {@link Node} (or a list of root nodes) back to STXT text. */
public final class NodeWriter {
    private NodeWriter() {}

    /** Indentation style to use when writing. */
    public enum IndentStyle {
        /** One tab character per level. */
        TABS,
        /** Four spaces per level. */
        SPACES_4
    }

    /**
     * Serializes a node to STXT text, indented with tabs.
     *
     * @param node node to serialize (along with its children).
     * @return the node serialized to STXT text, indented with tabs.
     */
    public static String toSTXT(Node node) {
        return toSTXT(node, IndentStyle.TABS);
    }

    /**
     * Serializes a node to STXT text, with the given indentation style.
     *
     * @param node node to serialize (along with its children).
     * @param style indentation style to use.
     * @return the node serialized to STXT text.
     */
    public static String toSTXT(Node node, IndentStyle style) {
        StringBuilder out = new StringBuilder(256);
        writeNode(out, node, 0, style);
        return out.toString();
    }

    /**
     * Serializes a list of root nodes to STXT text, indented with tabs.
     *
     * @param docs root nodes to serialize.
     * @return the documents serialized to STXT text, indented with tabs.
     */
    public static String toSTXT(List<Node> docs) {
        return toSTXT(docs, IndentStyle.TABS);
    }

    /**
     * Serializes a list of root nodes to STXT text, with the given indentation style.
     *
     * @param docs root nodes to serialize.
     * @param style indentation style to use.
     * @return the documents serialized to STXT text.
     */
    public static String toSTXT(List<Node> docs, IndentStyle style) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            if (i > 0) out.append('\n');
            writeNode(out, docs.get(i), 0, style);
        }
        return out.toString();
    }

    private static void writeNode(StringBuilder out, Node n, int depth, IndentStyle style) {
        indent(out, depth, style);

        // The namespace is written where the node declares it; inherited ones are implicit,
        // exactly as in the source (the effective namespace is the same either way)
        String ns = n.getDeclaredNamespace();

        out.append(n.getName());
        if (!ns.isEmpty())
        	out.append(" (").append(ns).append(')');

        if (n instanceof TextNode text) {
            out.append(" >>\n");

            for (String line : text.getTextLines()) {
                indent(out, depth + 1, style);
                out.append(line).append('\n');
            }
        } else if (n instanceof InlineNode inline) {
            out.append(":");
            String value = inline.getValue();
            if (!value.isEmpty()) out.append(' ').append(value);
            out.append('\n');
        }

        for (Node child : n.getChildren()) {
            writeNode(out, child, depth + 1, style);
        }
    }

    private static void indent(StringBuilder out, int depth, IndentStyle style) {
        if (depth <= 0) return;

        if (style == IndentStyle.SPACES_4) {
            for (int i = 0; i < depth; i++) 
            	out.append("    ");
        } else {
            for (int i = 0; i < depth; i++) 
            	out.append('\t');
        }
    }
}
