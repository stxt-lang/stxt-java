package dev.stxt;

/** Resultado de separar un nombre de nodo en bruto en su nombre y su namespace ya resueltos. */
public final class NameNamespace {
    private final String name;
    private final String namespace;

    /**
     * @param name nombre del nodo sin la parte de namespace.
     * @param namespace namespace resuelto (propio o heredado).
     */
    public NameNamespace(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    /** @return nombre del nodo, sin la parte de namespace. */
    public String getName() {
        return name;
    }

    /** @return namespace resuelto (propio o heredado del padre), o cadena vacía si no tiene. */
    public String getNamespace() {
        return namespace;
    }
}
