package dev.stxt.utils;

import java.io.File;

public class FileLoction {
	public static final File getFileBase(String dir) {
		if (dir.isEmpty()) new File("src/test/resources");
		return new File("src/test/resources/" + dir);
	}
	
}
