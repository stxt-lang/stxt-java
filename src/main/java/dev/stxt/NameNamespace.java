package dev.stxt;

public final class NameNamespace {
    private final String name;
    private final String namespace;

    public NameNamespace(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }
}
