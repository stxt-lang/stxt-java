package test;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Schema;

public final class JSON {
	private static final String IDENTER_STRING = "    "; // It could also be "\t"

	// Mixin: 'description' is a new, optional field in NodeDefinition; it is left out when it is
	// null so the existing fixtures, which do not declare it, keep working. The other fields
	// (e.g. min/max in ChildDefinition) still serialize null explicitly, as they always did.
	private abstract static class NodeDefinitionMixin {
		@JsonInclude(JsonInclude.Include.NON_NULL)
		abstract String getDescription();

		// The fixtures predate 1.0 and keep the historical 'normalized_name' member for the
		// canonical name (the API was renamed getCanonicalName() in 0.7.0, alias removed in 0.11.0).
		@JsonProperty("normalized_name")
		abstract String getCanonicalName();
	}

	// Same for the description of the Schema itself (0.11.0): left out when null.
	private abstract static class SchemaMixin {
		@JsonInclude(JsonInclude.Include.NON_NULL)
		abstract String getDescription();
	}

	private abstract static class ChildDefinitionMixin {
		@JsonProperty("normalized_name")
		abstract String getCanonicalName();
	}

	/**
	 * Serializes a {@link Node} in the historical shape of the {@code docs_json/} fixtures, which
	 * predate the 0.7.0 node model: the same members for both forms (an empty {@code value} for
	 * text nodes, an empty {@code text_lines} for inline ones), no {@code parent} (which would be a
	 * cycle) and no members added since. Bean serialization of the current classes would produce a
	 * different, cyclic shape; this keeps the fixtures valid as regression data. The canonical tree
	 * of STXT-TREE-SPEC is a different, normative serialization ({@code dev.stxt.runtime.TreeJson}).
	 */
	private static final class LegacyNodeSerializer extends JsonSerializer<Node> {
		@Override
		public void serialize(Node node, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeStartObject();
			gen.writeStringField("name", node.getName());
			gen.writeStringField("normalized_name", node.getCanonicalName());
			gen.writeStringField("namespace", node.getNamespace());
			gen.writeBooleanField("text_node", node.isTextNode());
			gen.writeStringField("value", node instanceof InlineNode inline ? inline.getValue() : "");
			gen.writeArrayFieldStart("text_lines");
			if (node instanceof TextNode text)
				for (String line : text.getTextLines())
					gen.writeString(line);
			gen.writeEndArray();
			gen.writeNumberField("line", node.getLine());
			gen.writeNumberField("level", node.getLevel());
			gen.writeArrayFieldStart("children");
			if (node instanceof InlineNode inline)
				for (Node child : inline.getChildren())
					serialize(child, gen, provider);
			gen.writeEndArray();
			gen.writeStringField("qualified_name", node.getQualifiedName());
			gen.writeStringField("text", node.getText());
			gen.writeEndObject();
		}
	}

	private static ObjectMapper configure(ObjectMapper mapper) {
		return mapper
				.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.addMixIn(NodeDefinition.class, NodeDefinitionMixin.class)
				.addMixIn(ChildDefinition.class, ChildDefinitionMixin.class)
				.addMixIn(Schema.class, SchemaMixin.class)
				.registerModule(new SimpleModule().addSerializer(Node.class, new LegacyNodeSerializer()));
	}

	private static final ObjectMapper MAPPER = configure(new ObjectMapper());

	// Shared pretty-print configuration (tab indentation)
	private static final DefaultIndenter INDENTER = new DefaultIndenter(IDENTER_STRING, DefaultIndenter.SYS_LF);
	private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter().withObjectIndenter(INDENTER)
			.withArrayIndenter(INDENTER);

	private static final ObjectMapper MAPPER_PRETTY = createPrettyMapper();

	private static ObjectMapper createPrettyMapper() {
		ObjectMapper mapper = configure(new ObjectMapper()).enable(SerializationFeature.INDENT_OUTPUT);

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