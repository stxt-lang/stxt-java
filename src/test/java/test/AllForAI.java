package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllForAI {
	public static void main(String[] args) {
		try {
			String content = readAllSrcMainJava();
			System.out.println(content);
			FileUtils.writeStringToFile(content, new File("all.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recursively reads every file under "src/main/java" (relative to the current working
	 * directory) and concatenates their content into a single String.
	 *
	 * Output format per file:
	 * File: relative/path/from/src/main/java/Name.java
	 * ```
	 * <content>
	 * ```
	 */
	public static String readAllSrcMainJava() throws IOException {
		Path root = Paths.get("src", "main", "java");

		if (!Files.exists(root) || !Files.isDirectory(root)) {
			return ""; // or throw an exception, if you prefer
		}

		try (Stream<Path> paths = Files.walk(root)) {
			return paths
					.filter(Files::isRegularFile)
					.map(path -> {
						try {
							// File name relative to src/main/java
							Path relative = root.relativize(path);
							String fileName = relative.toString().replace('\\', '/');

							byte[] bytes = Files.readAllBytes(path);
							String content = new String(bytes, StandardCharsets.UTF_8);

							String lineSep = System.lineSeparator();
							return "File: " + fileName + lineSep
									+ "```" + lineSep
									+ content + lineSep
									+ "```" + lineSep + lineSep;
						} catch (IOException e) {
							System.err.println("Could not read the file: " + path + " -> " + e.getMessage());
							return "";
						}
					})
					// they already include line breaks at the end of each block
					.collect(Collectors.joining());
		}
	}
}