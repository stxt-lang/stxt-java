package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

import dev.stxt.Constants;
import dev.stxt.InlineNode;
import dev.stxt.Parser;
import test.Corpus;

/** {@link Constants#SPEC_VERSION}: the version of the specifications, not of the artifact. */
class ConstantsTest {

	@Test
	void specVersionIsOneDotZero() {
		assertEquals("1.0", Constants.SPEC_VERSION);
	}

	/** The constant is tied to the version STXT-SPEC declares in its own Metadata (es/stxt-core-ref.stxt). */
	@Test
	void specVersionEqualsTheVersionDeclaredBySpec() {
		File file = new File(new File(Corpus.findStxtWeb(), "es"), "stxt-core-ref.stxt");
		InlineNode root = (InlineNode) new Parser().parse(Corpus.read(file)).get(0);
		InlineNode metadata = (InlineNode) root.getChild("Metadata");
		InlineNode version = (InlineNode) metadata.getChild("Version");

		assertNotNull(version, "STXT-SPEC has no Metadata/Version");
		assertEquals(version.getValue(), Constants.SPEC_VERSION);
	}
}
