package dev.stxt.resources;

public class NotFoundException extends ResourcesException {
	private static final long serialVersionUID = 1L;

	private final String namespace;
	private final String resource;

	public NotFoundException(String namespace, String resource) {
		super("Not found '" + resource + "' in namespace: " + namespace);
		this.namespace = namespace;
		this.resource = resource;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getResource() {
		return resource;
	}
}
