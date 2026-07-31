package dev.stxt.exceptions;

/** A {@link dev.stxt.resources.ResourcesLoader} did not find the requested resource for a namespace. */
public class ResourceNotFoundException extends STXTException {
	private static final long serialVersionUID = 1L;

	/** Namespace the resource was being looked up for. */
	private final String namespace;
	/** Name of the resource looked up (e.g. {@code schema} or {@code template}). */
	private final String resource;

	/**
	 * Creates the exception for a resource that could not be found.
	 *
	 * @param namespace namespace the resource was being looked up for.
	 * @param resource name of the resource looked up.
	 */
	public ResourceNotFoundException(String namespace, String resource) {
		super("RESOURCE_NOT_FOUND", "Not found '" + resource + "' in namespace: " + namespace);
		this.namespace = namespace;
		this.resource = resource;
	}

	/** {@return the namespace the resource was being looked up for} */
	public String getNamespace() {
		return namespace;
	}

	/** {@return the name of the resource looked up} */
	public String getResource() {
		return resource;
	}
}
