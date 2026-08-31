package dev.stxt.exceptions;

/** A {@link dev.stxt.resources.ResourcesLoader} did not find the requested resource for a namespace. */
public class ResourceNotFoundException extends STXTException {
	private static final long serialVersionUID = 1L;

	/** Namespace of the definition language the resource was looked up in (e.g. {@code @stxt.schema}). */
	private final String namespace;
	/** Name of the resource looked up: the namespace whose definition was wanted (e.g. {@code com.example.docs}). */
	private final String resource;

	/**
	 * Creates the exception for a resource that could not be found.
	 *
	 * @param namespace namespace of the definition language the resource was looked up in.
	 * @param resource name of the resource looked up (the namespace whose definition was wanted).
	 */
	public ResourceNotFoundException(String namespace, String resource) {
		super("RESOURCE_NOT_FOUND", "Not found '" + resource + "' in namespace: " + namespace);
		this.namespace = namespace;
		this.resource = resource;
	}

	/** {@return the namespace of the definition language the resource was looked up in} */
	public String getNamespace() {
		return namespace;
	}

	/** {@return the name of the resource looked up: the namespace whose definition was wanted} */
	public String getResource() {
		return resource;
	}
}
