package dev.stxt.slots;

public interface SlotsTemplateProvider {
	String getTemplate(String name, String namespace, String renderProfile);
}
