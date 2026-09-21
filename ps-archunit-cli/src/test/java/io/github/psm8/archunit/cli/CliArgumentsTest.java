package io.github.psm8.archunit.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CliArgumentsTest {
	@Test
	void accepts_repeated_classpath_options_and_report_options() {
		CliArguments arguments = CliArguments.parse(new String[] {
				"--config", "architecture.yml",
				"--classes", "target/classes",
				"--classpath", "dep-a.jar",
				"--classpath", "dep-b",
				"--classpath-file", "classpath.txt",
				"--format", "json",
				"--output", "report.json"
		});

		assertEquals(Path.of("architecture.yml"), arguments.config());
		assertEquals(1, arguments.classInputs().size());
		assertEquals(2, arguments.classpathEntries().size());
		assertEquals(Path.of("classpath.txt"), arguments.classpathFile());
		assertEquals(ReportFormat.JSON, arguments.format());
		assertEquals(Path.of("report.json"), arguments.output());
	}

	@Test
	void requires_configuration_unless_version_requested() {
		assertThrows(ConfigurationException.class, () -> CliArguments.parse(new String[] {
				"--format", "text"
		}));
		assertEquals(true, CliArguments.parse(new String[] {"--version"}).versionRequested());
	}
}
