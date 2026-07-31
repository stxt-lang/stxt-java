package test;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.stxt.schema.NodeDefinition;

public final class JSON {
	private static final String IDENTER_STRING = "    "; // It could also be "\t"

	// Mixin: 'description' is a new, optional field in NodeDefinition; it is left out when it is
	// null so the existing fixtures, which do not declare it, keep working. The other fields
	// (e.g. min/max in ChildDefinition) still serialize null explicitly, as they always did.
	private abstract static class NodeDefinitionMixin {
		@JsonInclude(JsonInclude.Include.NON_NULL)
		abstract String getDescription();
	}

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.addMixIn(NodeDefinition.class, NodeDefinitionMixin.class);

	// Shared pretty-print configuration (tab indentation)
	private static final DefaultIndenter INDENTER = new DefaultIndenter(IDENTER_STRING, DefaultIndenter.SYS_LF);
	private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter().withObjectIndenter(INDENTER)
			.withArrayIndenter(INDENTER);

	private static final ObjectMapper MAPPER_PRETTY = createPrettyMapper();

	private static ObjectMapper createPrettyMapper() {
		ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.addMixIn(NodeDefinition.class, NodeDefinitionMixin.class)
				.enable(SerializationFeature.INDENT_OUTPUT);

		// Use shared pretty printer
		mapper.setDefaultPrettyPrinter(PRETTY_PRINTER);
		return mapper;
	}

	private JSON() {
		// prevent instantiation
	}

	public static final String toJson(Object obj) {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static final String toJsonPretty(Object obj) {
		try {
			return MAPPER_PRETTY.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode toJsonTree(Object obj) {
		return toJsonTree(toJson(obj));
	}
	
	
	public static JsonNode toJsonTree(String json) {
		try {
			return MAPPER.readTree(json);
		} catch (IOException e) {
			throw new RuntimeException("JSON parsing error", e);
		}
	}
}