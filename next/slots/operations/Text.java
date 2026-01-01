package dev.stxt.slots.operations;

import dev.stxt.Node;

public class Text extends AbstractOperation {
	public static final Text INSTANCE = new Text();
	
	private Text() {
	}
	
	@Override
	public String getName() {
		return "text";
	}

	@Override
	public Object execute(Object object, String param) {
		checkParamNull(param);
		Node node = checkIsNode(object);
		return node.getText();
	}
}
