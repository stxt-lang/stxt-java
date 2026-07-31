package dev.stxt.resources;

import java.io.File;

import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.exceptions.STXTIOException;
import dev.stxt.utils.FileUtils;

/** {@link ResourcesLoader} that looks resources up in {@code <dir>/<namespace>/<resource>.stxt}. */
public class ResourcesLoaderDirectory implements ResourcesLoader {
	private final File dir;

	/** @param dir path of the root resources directory. @throws STXTException if it does not exist or is not a directory. */
	public ResourcesLoaderDirectory(String dir) {
		this(new File(dir));
	}

	/** @param dirResources root resources directory. @throws STXTException if it does not exist or is not a directory. */
	public ResourcesLoaderDirectory(File dirResources) {
		this.dir = dirResources;
		if (!dir.exists() || !dir.isDirectory())
			throw new STXTException("RESOURCE_DIRECTORY_NOT_VALID",
					"Directory not valid: " + dir.getAbsolutePath());
	}

	@Override
	public String retrieve(String namespace, String resource) {
		// Get the file
		File file = new File(dir, namespace + '/' + resource + ".stxt");

		// Check that it exists
		if (!file.exists() || !file.isFile())
			throw new ResourceNotFoundException(namespace, resource);

		// Return its value
		try {
			return FileUtils.readFileContent(file);
		}
		catch (java.io.IOException e) {
			throw new STXTIOException(e);
		}		
	}
}
