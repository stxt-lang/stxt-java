package test;

import java.io.File;

public class FileTestLoction {
	public static final File getFile(String dir) {
		if (dir.isEmpty()) new File("src/test/resources");
		return new File("src/test/resources/" + dir);
	}
	
}
