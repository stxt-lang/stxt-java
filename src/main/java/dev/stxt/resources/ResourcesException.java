package dev.stxt.resources;

import dev.stxt.ParseException;

public class ResourcesException extends ParseException {
	private static final long serialVersionUID = 1L;

	public ResourcesException(String message) {
		super(0, "RESOURCES_EXCEPTION", message);
	}
}
