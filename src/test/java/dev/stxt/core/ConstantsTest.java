package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.Constants;
import test.Corpus;
import test.JSON;

/** {@link Constants#SPEC_VERSION}: the date of the STXT-SPEC text implemented, not the artifact version. */
class ConstantsTest {

	@Test
	void specVersionIsADate() {
		assertTrue(Constants.SPEC_VERSION.matches("\\d{4}-\\d{2}-\\d{2}"), Constants.SPEC_VERSION);
	}

	/**
	 * The specifications carry a date and a status, not a version number (STXT-SPEC §1.1), and
	 * conformance is declared against the kit: the constant is the date the kit pins for STXT-SPEC
	 * (conformance/manifest.json), not the Last modif of the specification, so an editorial change
	 * of the text does not touch the library.
	 */
	@Test
	void specVersionEqualsTheDateTheConformanceKitPins() {
		File file = new File(new File(Corpus.findStxtLang(), "conformance"), "manifest.json");
		JsonNode manifest = JSON.toJsonTree(Corpus.read(file));

		assertEquals(manifest.get("specifications").get("STXT-SPEC").asText(), Constants.SPEC_VERSION);
	}
}
