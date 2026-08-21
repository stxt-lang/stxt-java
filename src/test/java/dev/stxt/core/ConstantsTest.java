package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.stxt.Constants;

/** {@link Constants#SPEC_VERSION}: the version of the specifications, not of the artifact. */
class ConstantsTest {

	@Test
	void specVersionIsOneDotZero() {
		assertEquals("1.0", Constants.SPEC_VERSION);
	}
}
