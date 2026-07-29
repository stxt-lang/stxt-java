package dev.stxt.exceptions;

/** Un {@link dev.stxt.resources.ResourcesLoader} no encontró el recurso pedido para un namespace. */
public class ResourceNotFoundException extends STXTException {
	private static final long serialVersionUID = 1L;

	/** Namespace para el que se buscaba el recurso. */
	private final String namespace;
	/** Nombre del recurso buscado (p. ej. {@code schema} o {@code template}). */
	private final String resource;

	/**
	 * @param namespace namespace para el que se buscaba el recurso.
	 * @param resource nombre del recurso buscado.
	 */
	public ResourceNotFoundException(String namespace, String resource) {
		super("RESOURCE_NOT_FOUND", "Not found '" + resource + "' in namespace: " + namespace);
		this.namespace = namespace;
		this.resource = resource;
	}

	/** @return namespace para el que se buscaba el recurso. */
	public String getNamespace() {
		return namespace;
	}

	/** @return nombre del recurso buscado. */
	public String getResource() {
		return resource;
	}
}
