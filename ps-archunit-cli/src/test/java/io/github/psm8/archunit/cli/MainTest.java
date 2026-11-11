package io.github.psm8.archunit.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.ToolProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
		assertTrue(output.toString().contains("unknown")
				|| output.toString().contains("0.5.0"));
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
							+ "\"],\"pass\":true,\"violations\":[],\"advisories\":[]}\n",
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

	@Test
	void advisories_are_reported_without_failing_the_run() throws Exception {
		String basePackage = "io.github.psm8.archunit.fixtures.switchable.setter.named";
		Result result = run(basePackage, fixture("switchable", "setter", "named"));

		assertEquals(0, result.exitCode());
		assertTrue(result.stdout().contains("\"pass\":true,\"violations\":[],\"advisories\":["));
		assertTrue(result.stdout().contains("Account.setName(java.lang.String) is a setter"));
		assertFalse(result.stdout().contains("was violated"));
	}

	@Test
	void consumer_classes_outside_the_runner_classpath_are_loadable_by_rules() throws Exception {
		Path sources = Files.createTempDirectory("ps-archunit-sources-");
		Path classes = Files.createTempDirectory("ps-archunit-classes-");
		Path money = sources.resolve("com/acme/isolated/domain/Money.java");
		Files.createDirectories(money.getParent());
		Files.writeString(money, """
				package com.acme.isolated.domain;

				public sealed interface Money permits Money.Euro {
					long cents();

					default boolean isPositive() {
						return cents() > 0;
					}

					record Euro(long cents) implements Money {
					}
				}
				""");
		int compiled = ToolProvider.getSystemJavaCompiler().run(
				null, null, null, "-d", classes.toString(), money.toString());
		assertEquals(0, compiled);

		Result result = run("domainOriented", "com.acme.isolated", classes);

		assertEquals(0, result.exitCode(), result.stderr());
	}

	@Test
	void interfaces_without_default_methods_need_no_resolvable_supertypes() throws Exception {
		Path sources = Files.createTempDirectory("ps-archunit-sources-");
		Path classes = Files.createTempDirectory("ps-archunit-classes-");
		Path base = sources.resolve("org/lib/Base.java");
		Path repository = sources.resolve("com/acme/unresolved/domain/Repository.java");
		Files.createDirectories(base.getParent());
		Files.createDirectories(repository.getParent());
		Files.writeString(base, """
				package org.lib;

				public interface Base {
				}
				""");
		Files.writeString(repository, """
				package com.acme.unresolved.domain;

				public interface Repository extends org.lib.Base {
					long count();
				}
				""");
		int compiled = ToolProvider.getSystemJavaCompiler().run(
				null, null, null, "-d", classes.toString(), base.toString(), repository.toString());
		assertEquals(0, compiled);
		Files.delete(classes.resolve("org/lib/Base.class"));

		Result result = run("domainOriented", "com.acme.unresolved", classes);

		assertEquals(0, result.exitCode(), result.stderr());
	}

	@Test
	void unresolvable_supertypes_of_checked_interfaces_fail_with_a_classpath_hint()
			throws Exception {
		Path sources = Files.createTempDirectory("ps-archunit-sources-");
		Path classes = Files.createTempDirectory("ps-archunit-classes-");
		Path base = sources.resolve("org/lib/Base.java");
		Path pricing = sources.resolve("com/acme/linkage/domain/Pricing.java");
		Files.createDirectories(base.getParent());
		Files.createDirectories(pricing.getParent());
		Files.writeString(base, """
				package org.lib;

				public interface Base {
				}
				""");
		Files.writeString(pricing, """
				package com.acme.linkage.domain;

				public interface Pricing extends org.lib.Base {
					default long doubled() {
						return 2;
					}
				}
				""");
		int compiled = ToolProvider.getSystemJavaCompiler().run(
				null, null, null, "-d", classes.toString(), base.toString(), pricing.toString());
		assertEquals(0, compiled);
		Files.delete(classes.resolve("org/lib/Base.class"));

		Result result = run("domainOriented", "com.acme.linkage", classes);

		assertEquals(2, result.exitCode());
		assertTrue(result.stderr().contains("--classpath"), result.stderr());
	}

	@Test
	void setters_overriding_supertypes_that_cannot_be_inspected_are_exempt() throws Exception {
		Path sources = Files.createTempDirectory("ps-archunit-sources-");
		Path classes = Files.createTempDirectory("ps-archunit-classes-");
		Path contract = sources.resolve("org/extlib/Configurable.java");
		Path widget = sources.resolve("com/acme/opaque/model/Widget.java");
		Path plain = sources.resolve("com/acme/opaque/model/Plain.java");
		Files.createDirectories(contract.getParent());
		Files.createDirectories(widget.getParent());
		Files.writeString(contract, """
				package org.extlib;

				public interface Configurable {
					void setOption(String option);
				}
				""");
		Files.writeString(widget, """
				package com.acme.opaque.model;

				public class Widget implements org.extlib.Configurable {
					private String option;

					@Override
					public void setOption(String option) {
						this.option = option;
					}
				}
				""");
		Files.writeString(plain, """
				package com.acme.opaque.model;

				public class Plain {
					private String color;

					public void setColor(String color) {
						this.color = color;
					}
				}
				""");
		int compiled = ToolProvider.getSystemJavaCompiler().run(null, null, null,
				"-d", classes.toString(), contract.toString(), widget.toString(), plain.toString());
		assertEquals(0, compiled);
		Files.delete(classes.resolve("org/extlib/Configurable.class"));

		Result result = run("com.acme.opaque", classes);

		assertEquals(0, result.exitCode(), result.stderr());
		assertTrue(result.stdout().contains("Plain.setColor(java.lang.String) is a setter"));
		assertFalse(result.stdout().contains("Widget.setOption"));
	}

	@Test
	void rule_without_a_store_entry_reports_all_its_violations_as_new() throws Exception {
		Path classes = fixture("switchable", "staticfield", "invalid");
		Path store = Files.createTempDirectory("ps-archunit-store-").resolve("store");

		Result recorded = run(
				"io.github.psm8.archunit.fixtures.switchable.staticfield.invalid",
				classes,
				"--freeze-store", store.toString(), "--refreeze");
		Result otherRule = run(
				"io.github.psm8.archunit.fixtures.switchable.staticfield",
				classes,
				"--freeze-store", store.toString());
		Result otherRuleAgain = run(
				"io.github.psm8.archunit.fixtures.switchable.staticfield",
				classes,
				"--freeze-store", store.toString());

		assertEquals(0, recorded.exitCode());
		assertEquals(1, otherRule.exitCode(), otherRule.stderr());
		assertTrue(otherRule.stdout().contains("\"pass\":false"));
		assertTrue(otherRule.stdout().contains("is a static non-final field"), otherRule.stdout());
		assertEquals(1, otherRuleAgain.exitCode(), "the missing entry must not be recorded");
	}

	@Test
	void missing_freeze_store_fails_and_points_to_refreeze() throws Exception {
		Path store = Files.createTempDirectory("ps-archunit-store-").resolve("missing");
		Result result = run(
				"io.github.psm8.archunit.fixtures.switchable.staticfield.invalid",
				fixture("switchable", "staticfield", "invalid"),
				"--freeze-store", store.toString());

		assertEquals(2, result.exitCode());
		assertTrue(result.stderr().contains("--refreeze"));
		assertFalse(Files.exists(store));
	}

	@Test
	void refreeze_records_violations_and_later_runs_tolerate_them() throws Exception {
		String basePackage = "io.github.psm8.archunit.fixtures.switchable.staticfield.invalid";
		Path classes = fixture("switchable", "staticfield", "invalid");
		Path store = Files.createTempDirectory("ps-archunit-store-").resolve("store");

		Result unfrozen = run(basePackage, classes);
		Result recorded = run(basePackage, classes,
				"--freeze-store", store.toString(), "--refreeze");
		Result frozen = run(basePackage, classes, "--freeze-store", store.toString());

		assertEquals(1, unfrozen.exitCode());
		assertEquals(0, recorded.exitCode());
		assertTrue(Files.isDirectory(store));
		assertEquals(0, frozen.exitCode());
		assertTrue(frozen.stdout().contains("\"pass\":true"));
	}

	private static Path fixture(String... segments) {
		Path path = Path.of("..", "ps-archunit", "target", "test-classes",
				"io", "github", "psm8", "archunit", "fixtures");
		for (String segment : segments) {
			path = path.resolve(segment);
		}
		return path;
	}

	private static Result run(String basePackage, Path classes, String... extraArguments)
			throws Exception {
		return run("baseline", basePackage, classes, extraArguments);
	}

	private static Result run(
			String tier,
			String basePackage,
			Path classes,
			String... extraArguments)
			throws Exception {
		Path config = Files.createTempFile("ps-archunit-cli-", ".yml");
		try {
			Files.writeString(config, """
					schemaVersion: 1
					tier: %s
					basePackage: %s
					""".formatted(tier, basePackage));
			List<String> arguments = new ArrayList<>(List.of(
					"--config", config.toString(),
					"--classes", classes.toString(),
					"--format", "json"));
			arguments.addAll(List.of(extraArguments));
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			ByteArrayOutputStream stderr = new ByteArrayOutputStream();
			int exitCode = Main.run(
					arguments.toArray(String[]::new),
					new PrintStream(stdout),
					new PrintStream(stderr));
			return new Result(exitCode, stdout.toString(), stderr.toString());
		} finally {
			Files.deleteIfExists(config);
		}
	}

	private record Result(int exitCode, String stdout, String stderr) {
	}
}
