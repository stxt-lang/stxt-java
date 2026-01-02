package dev.stxt.runtime;

import java.util.List;

import dev.stxt.Node;

public final class NodeWriter {
    private NodeWriter() {}

    public enum IndentStyle {
        TABS,       // \t
        SPACES_4    // "    "
    }

    public static String toSTXT(Node node) {
        return toSTXT(node, IndentStyle.TABS);
    }

    public static String toSTXT(Node node, IndentStyle style) {
        StringBuilder out = new StringBuilder(256);
        writeNode(out, node, 0, style, ""); // root: namespace "vacío" como base
        return out.toString();
    }

    public static String toSTXT(List<Node> docs) {
        return toSTXT(docs, IndentStyle.TABS);
    }

    public static String toSTXT(List<Node> docs, IndentStyle style) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            if (i > 0) out.append('\n');
            writeNode(out, docs.get(i), 0, style, "");
        }
        return out.toString();
    }

    // ----------------- internals -----------------

    private static void writeNode(StringBuilder out, Node n, int depth, IndentStyle style, String parentNs) {
        indent(out, depth, style);

        String ns = safe(n.getNamespace());

        // Imprime "(ns)" solo si cambia respecto al padre (namespace heredado)
        if (!ns.isEmpty() && !ns.equals(parentNs)) {
            out.append(n.getName()).append(" (").append(ns).append(')');
        } else {
            out.append(n.getName());
        }

        if (n.isTextNode()) {
            out.append(" >>\n");

            for (String line : n.getTextLines()) {
                indent(out, depth + 1, style);
                out.append(line == null ? "" : line);
                out.append('\n');
            }
        } else {
            out.append(":");
            String value = n.getValue();
            if (value != null && !value.isEmpty()) out.append(' ').append(value);
            out.append('\n');
        }

        for (Node child : n.getChildren()) {
            writeNode(out, child, depth + 1, style, ns);
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

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
