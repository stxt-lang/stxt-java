package dev.stxt.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;

/**
 * Converts parsed STXT nodes to the canonical tree defined by STXT-TREE-SPEC, as plain
 * {@code java.util} maps and lists ({@link #toCanonicalTree}), and serializes it as JSON
 * ({@link #toCanonicalJson}). This class has no JSON-library dependency: JSON is part of the
 * public representation, while the published core artifact intentionally has no runtime
 * dependencies. Same contract as {@code toCanonicalTree}/{@code toCanonicalJson} of the other ports.
 */
public final class TreeJson {
    private static final String INDENT = "  ";

    private TreeJson() {
    }

    /**
     * Converts every root node of an already parsed document to the logical tree defined by
     * STXT-TREE-SPEC. Each node is a {@link Map} with insertion order {@code name},
     * {@code canonicalName}, {@code namespace}, {@code form} and then {@code value} +
     * {@code children} (a list of nodes) for {@code "inline"} or {@code lines} (a list of
     * strings) for {@code "block"}. Source positions, indentation style, comments and derived
     * fields are deliberately absent.
     *
     * @param nodes root nodes of an already parsed document.
     * @return the canonical document tree, ready to be handed to any JSON library.
     */
    public static List<Map<String, Object>> toCanonicalTree(List<Node> nodes) {
        List<Map<String, Object>> result = new ArrayList<>(nodes.size());
        for (Node node : nodes) result.add(toCanonicalNode(node));
        return result;
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
     * Serializes the canonical tree of an already parsed document as human-readable JSON.
     * JSON whitespace is not part of STXT-TREE-SPEC; two-space indentation is this
     * implementation's deterministic presentation.
     *
     * @param nodes root nodes of an already parsed document.
     * @return canonical JSON without a final line break.
     */
    public static String toCanonicalJson(List<Node> nodes) {
        StringBuilder out = new StringBuilder(256);
        appendNodes(out, toCanonicalTree(nodes), 0);
        return out.toString();
    }

    private static Map<String, Object> toCanonicalNode(Node node) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", node.getName());
        result.put("canonicalName", node.getCanonicalName());
        result.put("namespace", node.getNamespace());
        if (node instanceof TextNode text) {
            result.put("form", "block");
            result.put("lines", new ArrayList<>(text.getTextLines()));
        } else {
            InlineNode inline = (InlineNode) node;
            result.put("form", "inline");
            result.put("value", inline.getValue());
            result.put("children", toCanonicalTree(inline.getChildren()));
        }
        return result;
    }

    private static void appendNodes(StringBuilder out, List<Map<String, Object>> nodes, int depth) {
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

    @SuppressWarnings("unchecked")
    private static void appendNode(StringBuilder out, Map<String, Object> node, int depth) {
        out.append("{\n");
        appendMember(out, depth + 1, "name", (String) node.get("name"), true);
        appendMember(out, depth + 1, "canonicalName", (String) node.get("canonicalName"), true);
        appendMember(out, depth + 1, "namespace", (String) node.get("namespace"), true);
        appendMember(out, depth + 1, "form", (String) node.get("form"), true);

        if ("block".equals(node.get("form"))) {
            indent(out, depth + 1);
            out.append("\"lines\": ");
            appendStrings(out, (List<String>) node.get("lines"), depth + 1);
            out.append('\n');
            indent(out, depth);
            out.append('}');
            return;
        }

        indent(out, depth + 1);
        out.append("\"value\": ");
        appendString(out, (String) node.get("value"));
        out.append(",\n");

        indent(out, depth + 1);
        out.append("\"children\": ");
        appendNodes(out, (List<Map<String, Object>>) node.get("children"), depth + 1);
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
