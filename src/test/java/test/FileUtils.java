package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	public static List<File> getStxtFiles(File directory) throws IOException {
		List<File> result = new ArrayList<File>();
		
		File[] files = directory.listFiles();
		for (File f: files)
			if (f.isFile() && f.getName().endsWith(".stxt"))
				result.add(f);
		
		return result;
	}
	public static void writeStringToFile(String content, File file) throws IOException {
		// Ensure parent directories exist
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			if (!parent.mkdirs() && !parent.exists()) {
				throw new IOException("Unable to create directories for file: " + file.getAbsolutePath());
			}
		}

		// Write content as UTF-8
		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
	}
	public static String readFileContent(File file) throws IOException {
		return dev.stxt.utils.FileUtils.readFileContent(file);
	}
}
