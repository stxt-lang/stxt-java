package dev.stxt.schema;

class SchemaChild {
	public String name;
	public String namespace;
	public Integer min = null;
	public Integer max = null;
	public String count = null;

	public String getQualifiedName() {
	    return namespace == null || namespace.isEmpty() ? name : namespace + ":" + name;
	}
}
