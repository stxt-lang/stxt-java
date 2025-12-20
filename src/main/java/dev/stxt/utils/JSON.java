package dev.stxt.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JSON {
	private static final String IDENTER_STRING = "    "; // También podría ser "\t"
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

	// Shared pretty-print configuration (tab indentation)
	private static final DefaultIndenter INDENTER = new DefaultIndenter(IDENTER_STRING, DefaultIndenter.SYS_LF);
	private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter().withObjectIndenter(INDENTER)
			.withArrayIndenter(INDENTER);

	private static final ObjectMapper MAPPER_PRETTY = createPrettyMapper();

	private static ObjectMapper createPrettyMapper() {
		ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
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

	public static JsonNode toJsonTree(String json) {
		try {
			return MAPPER.readTree(json);
		} catch (IOException e) {
			throw new RuntimeException("JSON parsing error", e);
		}
	}
}