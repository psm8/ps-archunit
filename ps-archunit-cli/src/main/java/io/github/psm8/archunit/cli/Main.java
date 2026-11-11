package io.github.psm8.archunit.cli;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import io.github.psm8.archunit.BaselineArchitectureRules;
import io.github.psm8.archunit.BaselineLayout;
import io.github.psm8.archunit.DomainOrientedArchitectureRules;
import io.github.psm8.archunit.DomainOrientedLayout;
import io.github.psm8.archunit.HexagonalArchitectureRules;
import io.github.psm8.archunit.HexagonalLayout;

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
			List<String> violations = findings(verificationRule(configuration, options), classes);
			List<String> advisories = findings(advisoryRule(configuration), classes);
			writeReport(options, stdout, new ArchitectureReport(
					Version.current(),
					configuration.tier().yamlName(),
					paths(inputs),
					violations.isEmpty(),
					violations,
					advisories));
			return violations.isEmpty() ? 0 : 1;
		} catch (IOException | RuntimeException failure) {
			stderr.println("ps-archunit: " + message(failure));
			return 2;
		}
	}

	private static ArchRule verificationRule(
			ParsedConfiguration configuration,
			CliArguments options) {
		ArchRule rule = switch (configuration.tier()) {
			case BASELINE -> BaselineArchitectureRules.baseline(
					(BaselineLayout) configuration.layout());
			case DOMAIN_ORIENTED -> DomainOrientedArchitectureRules.domainOriented(
					(DomainOrientedLayout) configuration.layout());
			case HEXAGONAL -> HexagonalArchitectureRules.hexagonal(
					(HexagonalLayout) configuration.layout());
		};
		if (options.freezeStore() == null) {
			return rule;
		}
		return FreezingArchRule.freeze(rule)
				.persistIn(new CommittedViolationStore(options.freezeStore(), options.refreeze()));
	}

	private static ArchRule advisoryRule(ParsedConfiguration configuration) {
		return switch (configuration.tier()) {
			case BASELINE -> BaselineArchitectureRules.baselineAdvisories(
					(BaselineLayout) configuration.layout());
			case DOMAIN_ORIENTED -> DomainOrientedArchitectureRules.domainOrientedAdvisories(
					(DomainOrientedLayout) configuration.layout());
			case HEXAGONAL -> HexagonalArchitectureRules.hexagonalAdvisories(
					(HexagonalLayout) configuration.layout());
		};
	}

	private static List<String> findings(ArchRule rule, JavaClasses classes) {
		try {
			rule.check(classes);
			return List.of();
		} catch (AssertionError failure) {
			return lines(failure);
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

	private static List<String> lines(AssertionError failure) {
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
