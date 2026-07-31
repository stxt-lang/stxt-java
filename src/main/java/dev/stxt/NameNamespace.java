package dev.stxt;

/** Result of splitting a raw node name into its resolved name and namespace. */
public final class NameNamespace {
    private final String name;
    private final String namespace;

    /**
     * Creates a resolved name and namespace pair.
     *
     * @param name name of the node without the namespace part.
     * @param namespace resolved namespace (its own or inherited).
     */
    public NameNamespace(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    /** {@return the name of the node, without the namespace part} */
    public String getName() {
        return name;
    }

    /** {@return the resolved namespace (its own or inherited from the parent), or the empty string if it has none} */
    public String getNamespace() {
        return namespace;
    }
}
