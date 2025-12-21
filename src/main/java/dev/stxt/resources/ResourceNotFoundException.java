package dev.stxt.resources;

import dev.stxt.STXTException;

public class ResourceNotFoundException extends STXTException {
	private static final long serialVersionUID = 1L;

	private final String namespace;
	private final String resource;

	public ResourceNotFoundException(String namespace, String resource) {
		super("RESOURCE_NOT_FOUND", "Not found '" + resource + "' in namespace: " + namespace);
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
