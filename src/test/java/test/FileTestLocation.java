package test;

import java.io.File;

public class FileTestLocation {
	public static File getFile(String dir) {
		if (dir.isEmpty()) return new File("src/test/resources");
		return new File("src/test/resources/" + dir);
	}
	
}
