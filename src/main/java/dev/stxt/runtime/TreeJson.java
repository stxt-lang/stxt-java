package dev.stxt.runtime;

import java.util.List;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;

/**
 * Serializes parsed STXT nodes to the canonical JSON tree defined by STXT-TREE-SPEC.
 * This class has no JSON-library dependency: JSON is part of the public representation,
 * while the published core artifact intentionally has no runtime dependencies.
 */
public final class TreeJson {
    private static final String INDENT = "  ";

    private TreeJson() {
    }

    /**
     * Serializes one root node as a one-element canonical STXT document tree.
     *
     * @param node root node of an already parsed document.
     * @return canonical JSON without a final line break.
     */
    public static String toCanonicalJson(Node node) {
        return toCanonicalJson(List.of(node));
    }

    /**
     * Serializes every root node of an already parsed document as canonical JSON.
     * Source positions, indentation style, comments and derived fields are deliberately
     * absent; see STXT-TREE-SPEC.
     *
     * @param nodes root nodes of an already parsed document.
     * @return canonical JSON without a final line break.
     */
    public static String toCanonicalJson(List<Node> nodes) {
        StringBuilder out = new StringBuilder(256);
        appendNodes(out, nodes, 0);
        return out.toString();
    }

    private static void appendNodes(StringBuilder out, List<Node> nodes, int depth) {
        if (nodes.isEmpty()) {
            out.append("[]");
            return;
        }

        out.append("[\n");
        for (int i = 0; i < nodes.size(); i++) {
            indent(out, depth + 1);
            appendNode(out, nodes.get(i), depth + 1);
            if (i + 1 < nodes.size()) out.append(',');
            out.append('\n');
        }
        indent(out, depth);
        out.append(']');
    }

    private static void appendNode(StringBuilder out, Node node, int depth) {
        out.append("{\n");
        appendMember(out, depth + 1, "name", node.getName(), true);
        appendMember(out, depth + 1, "canonicalName", node.getCanonicalName(), true);
        appendMember(out, depth + 1, "namespace", node.getNamespace(), true);
        appendMember(out, depth + 1, "form", node.isTextNode() ? "block" : "inline", true);

        if (node instanceof TextNode text) {
            indent(out, depth + 1);
            out.append("\"lines\": ");
            appendStrings(out, text.getTextLines(), depth + 1);
            out.append('\n');
            indent(out, depth);
            out.append('}');
            return;
        }

        indent(out, depth + 1);
        out.append("\"value\": ");
        appendString(out, ((InlineNode) node).getValue());
        out.append(",\n");

        indent(out, depth + 1);
        out.append("\"children\": ");
        appendNodes(out, node.getChildren(), depth + 1);
        out.append('\n');
        indent(out, depth);
        out.append('}');
    }

    private static void appendMember(StringBuilder out, int depth, String name, String value, boolean comma) {
        indent(out, depth);
        appendString(out, name);
        out.append(": ");
        appendString(out, value);
        if (comma) out.append(',');
        out.append('\n');
    }

    private static void appendStrings(StringBuilder out, List<String> values, int depth) {
        if (values.isEmpty()) {
            out.append("[]");
            return;
        }

        out.append("[\n");
        for (int i = 0; i < values.size(); i++) {
            indent(out, depth + 1);
            appendString(out, values.get(i));
            if (i + 1 < values.size()) out.append(',');
            out.append('\n');
        }
        indent(out, depth);
        out.append(']');
    }

    private static void appendString(StringBuilder out, String value) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append("\\u");
                        String hex = Integer.toHexString(c);
                        for (int pad = hex.length(); pad < 4; pad++) out.append('0');
                        out.append(hex);
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
    }

    private static void indent(StringBuilder out, int depth) {
        for (int i = 0; i < depth; i++) out.append(INDENT);
    }
}
