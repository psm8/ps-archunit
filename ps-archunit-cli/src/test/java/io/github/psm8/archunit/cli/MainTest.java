package io.github.psm8.archunit.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
	@Test
	void version_does_not_require_configuration() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		int exitCode = Main.run(
				new String[] {"--version"},
				new PrintStream(output),
				new PrintStream(new ByteArrayOutputStream()));

		assertEquals(0, exitCode);
		assertTrue(output.toString().contains("unknown") || output.toString().contains("0.2.0"));
	}

	@Test
	void passing_architecture_is_reported_as_stable_json() throws Exception {
		Path config = Files.createTempFile("ps-archunit-cli-", ".yml");
		try {
			Files.writeString(config, """
					schemaVersion: 1
					tier: baseline
					basePackage: io.github.psm8.archunit.fixtures.valid
					""");
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Path classes = Path.of(
					"..",
					"ps-archunit",
					"target",
					"test-classes",
					"io",
					"github",
					"psm8",
					"archunit",
					"fixtures",
					"valid");

			int exitCode = Main.run(
					new String[] {
							"--config", config.toString(),
							"--classes", classes.toString(),
							"--format", "json"
					},
					new PrintStream(output),
					new PrintStream(new ByteArrayOutputStream()));

			assertEquals(0, exitCode);
			assertEquals(
					"{\"version\":\"unknown\",\"tier\":\"baseline\",\"input\":[\""
							+ classes.toString().replace("\\", "\\\\")
							+ "\"],\"pass\":true,\"violations\":[]}\n",
					output.toString());
		} finally {
			Files.deleteIfExists(config);
		}
	}

	@Test
	void classpath_entries_are_imported_and_reported_with_analysis_inputs() throws Exception {
		Path config = Files.createTempFile("ps-archunit-cli-", ".yml");
		try {
			Files.writeString(config, """
					schemaVersion: 1
					tier: baseline
					basePackage: io.github.psm8.archunit.fixtures.valid
					""");
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Path classes = Path.of(
					"..",
					"ps-archunit",
					"target",
					"test-classes",
					"io",
					"github",
					"psm8",
					"archunit",
					"fixtures",
					"valid");

			int exitCode = Main.run(
					new String[] {
							"--config", config.toString(),
							"--classes", classes.toString(),
							"--classpath", classes.toString(),
							"--format", "json"
					},
					new PrintStream(output),
					new PrintStream(new ByteArrayOutputStream()));

			String escapedClasses = classes.toString().replace("\\", "\\\\");
			assertEquals(0, exitCode);
			assertTrue(output.toString().contains(
					"\"input\":[\"" + escapedClasses + "\",\"" + escapedClasses + "\"]"));
		} finally {
			Files.deleteIfExists(config);
		}
	}
}
