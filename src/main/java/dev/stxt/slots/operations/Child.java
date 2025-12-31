package dev.stxt.slots.operations;

import dev.stxt.Node;

public class Child extends AbstractOperation {
	public static final Child INSTANCE = new Child();
	
	private Child() {
	}
	
	@Override
	public String getName() {
		return "child";
	}
	
	@Override
	public Object execute(Object object, String param) {
		checkParamNotNull(param);
		Node node = checkIsNode(object);
		return node.getChild(param);
	}
}
