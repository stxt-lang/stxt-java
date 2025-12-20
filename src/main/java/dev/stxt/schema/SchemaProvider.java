package dev.stxt.schema;

import dev.stxt.IOException;

import dev.stxt.ParseException;
import dev.stxt.resources.NotFoundException;

public interface SchemaProvider {
	public Schema getSchema(String namespace) throws IOException, ParseException, NotFoundException;
}
