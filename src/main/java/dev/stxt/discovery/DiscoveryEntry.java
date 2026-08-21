package dev.stxt.discovery;

import java.nio.file.Path;

/**
 * An entry of a directory listing, as returned by {@link DiscoveryFileSystem#listDirectory(Path)}.
 *
 * @param path full path of the entry, in the same form the file system uses for every other path.
 * @param name base name of the entry (last path segment).
 * @param isDirectory true if the entry is a directory.
 */
public record DiscoveryEntry(Path path, String name, boolean isDirectory) {
}
