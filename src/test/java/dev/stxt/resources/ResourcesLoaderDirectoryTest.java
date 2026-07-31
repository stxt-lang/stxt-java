package dev.stxt.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.STXTException;
import test.FileTestLoction;

public class ResourcesLoaderDirectoryTest {
	@Test
	void testExist() {
		ResourcesLoaderDirectory finder = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));

		String result = finder.retrieve("@stxt.schema", "com.example.docs");
		System.out.println("Result = " + result);
		assertNotNull(result, "We must find a result");
		assertTrue(result.startsWith("Schema (@stxt.schema): com.example.docs"), "It must start with the schema");
	}

	@Test
	void testNotFound() {
		ResourcesLoaderDirectory finder = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));

		try {
			finder.retrieve("@stxt.nonexistent", "com.example.nonexistent");
			System.out.println("ERROR! Not exception");
			fail("An exception was expected");
		} catch (ResourceNotFoundException nfr) {
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
			fail("An exception was expected");
		} catch (STXTException nfr) {
			System.out.println("OK exception");
		}
	}
}
