package transforms;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.schema.SchemaValidator;

public class DemoTransform 
{
	private static final String doc = """
# A simple document, with no validation and no namespace
Email (com.example.docs):
	From: John Smith
	To: Mery Adams
	Cc: Keyla Brown
	Title: Project report
	# The body is a literal text block
	Body Content >>
		Hello Mery!
		The book is finished!		
			""";
	
	private static final String grammar = """
Template (@stxt.template): com.example.docs
	Structure >>
		Email (com.example.docs):
			From: EMAIL
			To: EMAIL
			Cc: EMAIL
			Bcc: EMAIL
			Title: (?)
			Body Content: (1) TEXT
			Metadata (org.example.meta): (?)			
			""";
	
	public static void main(String[] args) {
		System.out.println("Inici");
		
		Parser p = new Parser();
		List<Node> nodes = p.parse(doc);
		
		for (Node node: nodes)
		{
			System.out.println(node);
		}		
		
		
		
		System.out.println("Fi");
	}
}
