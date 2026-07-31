package dev.stxt.resources;

/** Abstracts where resources ({@code @stxt.schema}/{@code @stxt.template}) come from, per namespace. */
public interface ResourcesLoader {
	/**
	 * Retrieves the content of a resource of a namespace.
	 *
	 * @param namespace namespace of the resource to look up.
	 * @param resource name of the resource (e.g. {@code schema} or {@code template}).
	 * @return the content of the resource.
	 * @throws dev.stxt.exceptions.ResourceNotFoundException if it does not exist.
	 */
	public String retrieve(String namespace, String resource);
}
