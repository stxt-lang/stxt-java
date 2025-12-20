package dev.stxt.resources;

import dev.stxt.IOException;

public interface ResourcesLoader {
	public String retrieve(String namespace, String resource) throws NotFoundException, IOException;
}
