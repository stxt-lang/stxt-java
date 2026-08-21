package dev.stxt.discovery;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Minimal file-system abstraction used by {@link DiscoveryResolver} (STXT-DISCOVERY-SPEC).
 *
 * The resolver only needs these three operations, so an implementation may back them with the
 * real file system ({@link NioDiscoveryFileSystem}), a {@code java.nio.file.FileSystem} over a
 * ZIP, or an in-memory tree for tests. Paths are {@link Path} values: the resolver derives parents
 * and children with {@link Path#getParent()} and {@link Path#resolve(String)}, so every path an
 * implementation returns must be comparable with {@link Path#equals(Object)}.
 */
public interface DiscoveryFileSystem {
	/**
	 * Whether a path exists and is a directory.
	 *
	 * @param path path to check.
	 * @return true if the path is an existing directory; false otherwise (including I/O errors).
	 */
	boolean isDirectory(Path path);

	/**
	 * Lists the immediate entries of a directory.
	 *
	 * @param path directory to list.
	 * @return the entries of the directory, in any order.
	 * @throws IOException if the directory cannot be listed.
	 */
	List<DiscoveryEntry> listDirectory(Path path) throws IOException;

	/**
	 * Reads a file as UTF-8 text.
	 *
	 * @param path file to read.
	 * @return the text content of the file.
	 * @throws IOException if the file cannot be read.
	 */
	String readFile(Path path) throws IOException;
}
