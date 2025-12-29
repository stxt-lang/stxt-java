package test;

import java.io.File;

public class AllJSONDelete {
	public static void main(String[] args) throws Exception {
		delete("docs_json");
		delete("error_docs_json");
		delete("error_schema_json");
		delete("error_template_json");
		delete("schema_json");
	}

	private static void delete(String dir) {
		File dirDelete = FileTestLoction.getFile(dir);
		for (File f: dirDelete.listFiles())
			f.delete();
	}
}
