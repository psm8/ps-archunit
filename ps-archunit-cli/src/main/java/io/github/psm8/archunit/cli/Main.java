package io.github.psm8.archunit.cli;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import io.github.psm8.archunit.BaselineArchitectureRules;
import io.github.psm8.archunit.DomainOrientedArchitectureRules;
import io.github.psm8.archunit.HexagonalArchitectureRules;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Main {
	private Main() {
	}

	public static void main(String[] arguments) {
		System.exit(run(arguments, System.out, System.err));
	}

	static int run(String[] arguments, PrintStream stdout, PrintStream stderr) {
		try {
			CliArguments options = CliArguments.parse(arguments);
			if (options.versionRequested()) {
				stdout.println(Version.current());
				return 0;
			}
			ParsedConfiguration configuration =
					new ConfigurationParser().parse(options.config());
			List<Path> inputs = validateInputs(options);
			JavaClasses classes = new ClassFileImporter().importPaths(inputs);
			if (classes.isEmpty()) {
				throw new ConfigurationException("class input contains no class files");
			}
			try {
				check(configuration, classes);
				writeReport(options, stdout, new ArchitectureReport(
						Version.current(),
						configuration.tier().yamlName(),
						paths(inputs),
						true,
						List.of()));
				return 0;
			} catch (AssertionError violation) {
				writeReport(options, stdout, new ArchitectureReport(
						Version.current(),
						configuration.tier().yamlName(),
						paths(inputs),
						false,
						violations(violation)));
				return 1;
			}
		} catch (IOException | RuntimeException failure) {
			stderr.println("ps-archunit: " + message(failure));
			return 2;
		}
	}

	private static void check(ParsedConfiguration configuration, JavaClasses classes) {
		switch (configuration.tier()) {
			case BASELINE -> BaselineArchitectureRules.baseline(
					(io.github.psm8.archunit.BaselineLayout) configuration.layout()).check(classes);
			case DOMAIN_ORIENTED -> DomainOrientedArchitectureRules.domainOriented(
					(io.github.psm8.archunit.DomainOrientedLayout) configuration.layout()).check(classes);
			case HEXAGONAL -> HexagonalArchitectureRules.hexagonal(
					(io.github.psm8.archunit.HexagonalLayout) configuration.layout()).check(classes);
		}
	}

	private static List<Path> validateInputs(CliArguments options) throws IOException {
		List<Path> inputs = new ArrayList<>(options.classInputs());
		inputs.addAll(options.classpathEntries());
		if (options.classpathFile() != null) {
			for (String line : Files.readAllLines(options.classpathFile())) {
				String value = line.trim();
				if (!value.isEmpty() && !value.startsWith("#")) {
					inputs.add(Path.of(value));
				}
			}
		}
		if (inputs.isEmpty()) {
			throw new ConfigurationException("at least one class input is required");
		}
		for (Path input : inputs) {
			if (!Files.exists(input)) {
				throw new ConfigurationException("input does not exist: " + input);
			}
			if (!Files.isDirectory(input) && !isJar(input)) {
				throw new ConfigurationException("input must be a directory or JAR: " + input);
			}
		}
		return List.copyOf(inputs);
	}

	private static boolean isJar(Path input) {
		return Files.isRegularFile(input)
				&& input.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".jar");
	}

	private static List<String> paths(List<Path> inputs) {
		return inputs.stream().map(Path::toString).toList();
	}

	private static List<String> violations(AssertionError failure) {
		String message = failure.getMessage();
		if (message == null || message.isBlank()) {
			return List.of(failure.toString());
		}
		return message.lines()
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.toList();
	}

	private static void writeReport(
			CliArguments options,
			PrintStream stdout,
			ArchitectureReport report)
			throws IOException {
		String rendered = report.render(options.format());
		if (options.output() == null) {
			stdout.print(rendered);
			return;
		}
		Files.writeString(options.output(), rendered);
	}

	private static String message(Exception failure) {
		return failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
	}
}
