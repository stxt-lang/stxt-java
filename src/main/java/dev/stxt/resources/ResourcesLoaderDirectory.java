package dev.stxt.resources;

import java.io.File;

import dev.stxt.IOException;
import dev.stxt.utils.FileUtils;

public class ResourcesLoaderDirectory implements ResourcesLoader {
	private final File dir;

	public ResourcesLoaderDirectory(String dir) {
		this(new File(dir));
	}

	public ResourcesLoaderDirectory(File dirResources) {
		this.dir = dirResources;
		if (!dir.exists() || !dir.isDirectory())
			throw new ResourcesException("Directory not valid: " + dir.getAbsolutePath());
	}

	@Override
	public String retrieve(String namespace, String resource) {
		// Obtenemos fichero
		File file = new File(dir, namespace + '/' + resource + ".stxt");

		// Validamos exista
		if (!file.exists() || !file.isFile())
			throw new NotFoundException(namespace, resource);

		// Retornamos valor
		try {
			return FileUtils.readFileContent(file);
		}
		catch (java.io.IOException e) {
			throw new IOException(e);
		}
		
	}
}
