package dev.stxt.runtime;

import java.util.List;

import dev.stxt.Node;

public final class NodeWriter {
    private NodeWriter() {}

    public enum IndentStyle {
        TABS,
        SPACES_4,
    }

    public static String toSTXT(Node node) {
        return toSTXT(node, IndentStyle.TABS);
    }

    public static String toSTXT(Node node, IndentStyle style) {
        StringBuilder out = new StringBuilder(256);
        writeNode(out, node, 0, style);
        return out.toString();
    }

    public static String toSTXT(List<Node> docs) {
        return toSTXT(docs, IndentStyle.TABS);
    }

    public static String toSTXT(List<Node> docs, IndentStyle style) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            if (i > 0) out.append('\n');
            writeNode(out, docs.get(i), 0, style);
        }
        return out.toString();
    }

    // ----------------- internals -----------------

    private static void writeNode(StringBuilder out, Node n, int depth, IndentStyle style) {
        indent(out, depth, style);

        // Nombre (con namespace explícito solo si es distinto del padre)
        // Como Node no guarda el namespace heredado, imprimimos siempre "Name (ns)" cuando hay namespace.
        // Si quieres el modo "heredado", lo implementamos con un parámetro parentNs.
        if (n.getNamespace() != null && !n.getNamespace().isEmpty()) {
            out.append(n.getName()).append(" (").append(n.getNamespace()).append(')');
        } else {
            out.append(n.getName());
        }

        if (n.isTextNode()) {
            out.append(" >>\n");

            // líneas de texto tal cual, con indent depth+1
            for (String line : n.getTextLines()) {
                indent(out, depth + 1, style);
                out.append(line == null ? "" : line);
                out.append('\n');
            }
        } else {
            out.append(":");
            if (!n.getValue().isEmpty()) out.append(' ').append(n.getValue());
            out.append('\n');
        }

        for (Node child : n.getChildren()) {
            writeNode(out, child, depth + 1, style);
        }
    }

    private static void indent(StringBuilder out, int depth, IndentStyle style) {
        if (depth <= 0) return;

        switch (style) {
            case TABS:
                for (int i = 0; i < depth; i++) out.append('\t');
                return;
            case SPACES_4:
            default:
                for (int i = 0; i < depth; i++) out.append("    ");
        }
    }
}
