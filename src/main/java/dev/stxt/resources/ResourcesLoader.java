package dev.stxt.resources;

/** Abstracts where resources ({@code @stxt.schema}/{@code @stxt.template}) come from, per namespace. */
public interface ResourcesLoader {
	/**
	 * Retrieves the content of a resource of a namespace.
	 *
	 * @param namespace namespace of the definition language the resource belongs to
	 *        (e.g. {@code @stxt.schema} or {@code @stxt.template}).
	 * @param resource name of the resource inside that namespace: the namespace whose
	 *        definition is being looked up (e.g. {@code com.example.docs}).
	 * @return the content of the resource.
	 * @throws dev.stxt.exceptions.ResourceNotFoundException if it does not exist.
	 */
	public String retrieve(String namespace, String resource);
}
