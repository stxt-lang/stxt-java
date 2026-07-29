package dev.stxt.resources;

/** Abstrae de dónde vienen los recursos ({@code @stxt.schema}/{@code @stxt.template}) por namespace. */
public interface ResourcesLoader {
	/**
	 * @param namespace namespace del recurso a buscar.
	 * @param resource nombre del recurso (p. ej. {@code schema} o {@code template}).
	 * @return el contenido del recurso.
	 * @throws dev.stxt.exceptions.ResourceNotFoundException si no existe.
	 */
	public String retrieve(String namespace, String resource);
}
