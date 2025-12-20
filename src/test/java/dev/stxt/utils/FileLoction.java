package dev.stxt.utils;

import java.io.File;

public class FileLoction {
	public static final File getFileBase(String dir) {
		return new File("src/test/resources/" + dir);
	}
	
}
