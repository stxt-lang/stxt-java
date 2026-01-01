package dev.stxt.slots;

import java.util.HashMap;
import java.util.Map;

import dev.stxt.slots.operations.Child;
import dev.stxt.slots.operations.Text;

public class OperationRegistry {
	private static final Map<String, Operation> OPERATIONS = new HashMap<String, Operation>();
	
	static {
		addOperation(Child.INSTANCE);
		addOperation(Text.INSTANCE);
	}

	private static void addOperation(Operation instance) {
		OPERATIONS.put(instance.getName(), instance);
	}
	
	public static Operation get(String operation) {
		return OPERATIONS.get(operation);
	}
}
