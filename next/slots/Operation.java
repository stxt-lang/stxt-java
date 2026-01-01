package dev.stxt.slots;

public interface Operation {
	public Object execute(Object object, String param);
	public String getName();
}
