package dev.stxt.resources;

import java.io.IOException;

public interface ResourcesLoader {
	public String retrieve(String namespace, String resource) throws NotFoundException, IOException;
}
