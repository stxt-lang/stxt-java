package dev.stxt.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import test.FileTestLoction;

public class ResourcesLoaderDirectoryTest {
	@Test
	void testExist() throws IOException, ResourcesException, NotFoundException {
		ResourcesLoaderDirectory finder = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));

		String result = finder.retrieve("@stxt.schema", "com.example.docs");
		System.out.println("Result = " + result);
		assertNotNull(result, "Debemos encontrar un resultado");
		assertTrue(result.startsWith("Schema (@stxt.schema): com.example.docs"), "Debe empezar por schema");
	}

	@Test
	void testNotFound() throws IOException, ResourcesException {
		ResourcesLoaderDirectory finder = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));

		try {
			finder.retrieve("@stxt.nonexistent", "com.example.nonexistent");
			System.out.println("ERROR! Not exception");
			fail("Se esperaba una excepción");
		} catch (NotFoundException nfr) {
			System.out.println("OK exception");
			assertEquals(nfr.getNamespace(), "@stxt.nonexistent");
			assertEquals(nfr.getResource(), "com.example.nonexistent");
		}
	}

	@Test
	void testInitError() {
		try {
			new ResourcesLoaderDirectory(new File("testnotexistent/stxt"));
			System.out.println("ERROR! Not exception");
			fail("Se esperaba una excepción");
		} catch (ResourcesException nfr) {
			System.out.println("OK exception");
		}
	}
}
