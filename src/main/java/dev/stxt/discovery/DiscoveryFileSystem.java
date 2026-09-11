package dev.stxt.discovery;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Minimal file-system abstraction used by {@link DiscoveryResolver} (STXT-DISCOVERY-SPEC).
 *
 * The resolver only needs these few operations, so an implementation may back them with the
 * real file system ({@link NioDiscoveryFileSystem}), a {@code java.nio.file.FileSystem} over a
 * ZIP, or an in-memory tree for tests. Paths are {@link Path} values: the resolver derives parents
 * and children with {@link Path#getParent()} and {@link Path#resolve(String)}, so every path an
 * implementation returns must be comparable with {@link Path#equals(Object)}.
 */
public interface DiscoveryFileSystem {
	/**
	 * Whether a path exists and is a directory. Follows symbolic links: a linked user level,
	 * system level or {@code STXT_PATH} entry is a directory (STXT-DISCOVERY-SPEC sections 4.2
	 * and 6).
	 *
	 * @param path path to check.
	 * @return true if the path is an existing directory; false otherwise (including I/O errors).
	 */
	boolean isDirectory(Path path);

	/**
	 * Whether a path is a symbolic link, whatever it points to and whether or not the target
	 * exists. Only consulted during the project-level ascent (STXT-DISCOVERY-SPEC section 4.1):
	 * the {@code .stxt} of an ancestor that is itself a link forms no level. The default answers
	 * false, for an implementation over an abstraction with no links (an in-memory tree, a ZIP)
	 * and for every implementation written before the operation existed.
	 *
	 * @param path path to check.
	 * @return true if the path is a symbolic link; false otherwise (including I/O errors).
	 */
	default boolean isSymbolicLink(Path path) {
		return false;
	}

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
