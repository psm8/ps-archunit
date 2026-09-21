package io.github.psm8.archunit.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

record CliArguments(
		Path config,
		List<Path> classInputs,
		List<Path> classpathEntries,
		Path classpathFile,
		ReportFormat format,
		Path output,
		boolean versionRequested) {
	static CliArguments parse(String[] arguments) {
		List<Path> classes = new ArrayList<>();
		List<Path> classpath = new ArrayList<>();
		Path config = null;
		Path classpathFile = null;
		Path output = null;
		ReportFormat format = ReportFormat.TEXT;
		boolean version = false;

		for (int index = 0; index < arguments.length; index++) {
			String argument = arguments[index];
			if (argument.equals("--version")) {
				version = true;
				continue;
			}
			String option = optionName(argument);
			String value = optionValue(argument, arguments, index);
			if (!argument.contains("=")) {
				index++;
			}
			switch (option) {
				case "--config" -> config = path(value, option);
				case "--classes" -> addPaths(classes, value, option);
				case "--classpath" -> addPaths(classpath, value, option);
				case "--classpath-file" -> classpathFile = path(value, option);
				case "--format" -> format = ReportFormat.parse(value);
				case "--output" -> output = path(value, option);
				default -> throw new ConfigurationException("unknown option: " + argument);
			}
		}
		if (!version && config == null) {
			throw new ConfigurationException("--config is required");
		}
		if (version) {
			return new CliArguments(config, List.copyOf(classes), List.copyOf(classpath),
					classpathFile, format, output, true);
		}
		if (classes.isEmpty()) {
			classes.addAll(defaultClassInputs());
		}
		return new CliArguments(config, List.copyOf(classes), List.copyOf(classpath),
				classpathFile, format, output, false);
	}

	private static String optionName(String argument) {
		int separator = argument.indexOf('=');
		return separator < 0 ? argument : argument.substring(0, separator);
	}

	private static String optionValue(String argument, String[] arguments, int index) {
		int separator = argument.indexOf('=');
		if (separator >= 0) {
			if (separator == argument.length() - 1) {
				throw new ConfigurationException(optionName(argument) + " requires a value");
			}
			return argument.substring(separator + 1);
		}
		if (index + 1 >= arguments.length || arguments[index + 1].startsWith("--")) {
			throw new ConfigurationException(argument + " requires a value");
		}
		return arguments[index + 1];
	}

	private static Path path(String value, String option) {
		if (value.isBlank()) {
			throw new ConfigurationException(option + " requires a non-blank path");
		}
		return Path.of(value);
	}

	private static void addPaths(List<Path> target, String value, String option) {
		for (String part : value.split(",", -1)) {
			target.add(path(part.trim(), option));
		}
	}

	private static List<Path> defaultClassInputs() {
		List<Path> defaults = new ArrayList<>();
		for (Path path : List.of(Path.of("target/classes"), Path.of("target/test-classes"))) {
			if (Files.exists(path)) {
				defaults.add(path);
			}
		}
		if (defaults.isEmpty()) {
			defaults.add(Path.of("target/classes"));
		}
		return defaults;
	}
}
