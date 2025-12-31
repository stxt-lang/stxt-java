package dev.stxt.slots.operations;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.exceptions.STXTException;
import dev.stxt.slots.Operation;

public abstract class AbstractOperation implements Operation {
	protected Node checkIsNode(Object object) {
		if (!(object instanceof Node)) throw new STXTException("OPERATION_NOT_SUPPORTED", "Input must be Node");
		return (Node) object;
	}	
	protected String checkIsText(Object object) {
		if (!(object instanceof String)) throw new STXTException("OPERATION_NOT_SUPPORTED", "Input must be Node");
		return (String) object;
	}	
	@SuppressWarnings("unchecked")
	protected List<Node> checkIsNodeList(Object object) {
		if (!(object instanceof List)) throw new STXTException("OPERATION_NOT_SUPPORTED", "Input must be Node");
		
		List<Node> list = (List<Node>) object;
		for (Object o: list)
			checkIsNode(o);
		
		return list;
	}
	protected void checkParamNotNull(String param) {
		if (param == null || param.isEmpty())
			throw new STXTException("OPERATION_NOT_SUPPORTED", "Input param must NOT be null");
	}
	protected void checkParamNull(String param) {
		if (param != null && !param.isEmpty())
			throw new STXTException("OPERATION_NOT_SUPPORTED", "Input param must be null");
	}
}
