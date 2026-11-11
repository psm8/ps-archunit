package io.github.psm8.archunit;

import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.psm8.archunit.BaselineArchitectureRules.baseline;
import static io.github.psm8.archunit.BaselineArchitectureRules.baselineAdvisories;
import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOriented;
import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOrientedAdvisories;
import static io.github.psm8.archunit.HexagonalArchitectureRules.hexagonal;
import static io.github.psm8.archunit.HexagonalArchitectureRules.hexagonalAdvisories;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitchableRulesTest {
	private static final String FIXTURES = "io.github.psm8.archunit.fixtures.switchable";
	private static final String NAMED_SETTER = FIXTURES + ".setter.named";
	private static final String FLUENT_SETTER = FIXTURES + ".setter.fluent";
	private static final String BEHAVIOR = FIXTURES + ".setter.behavior";
	private static final String FRAMEWORK_OVERRIDE = FIXTURES + ".setter.override";
	private static final String STATIC_FIELD = FIXTURES + ".staticfield.invalid";
	private static final String CONSTANTS = FIXTURES + ".staticfield.valid";
	private static final String INVALID_DEFAULTS = FIXTURES + ".defaultmethod.invalid";
	private static final String VALID_DEFAULTS = FIXTURES + ".defaultmethod.valid";
	private static final String PORT_DEFAULTS = FIXTURES + ".defaultmethod.port";

	@Test
	void setters_are_advisories_by_default() {
		assertDoesNotThrow(() -> baseline(NAMED_SETTER).check(imported(NAMED_SETTER)));

		String message = failureOf(baselineAdvisories(NAMED_SETTER), NAMED_SETTER);
		assertTrue(message.contains(NAMED_SETTER + ".model.Account.setName(java.lang.String) is a setter"));
	}

	@Test
	void setter_fail_mode_rejects_setters_in_the_baseline() {
		BaselineLayout layout = BaselineLayout.builder(NAMED_SETTER)
				.setterMode(RuleMode.FAIL)
				.build();

		String message = failureOf(baseline(layout), NAMED_SETTER);
		assertTrue(message.contains("setName(java.lang.String) is a setter"));
		assertDoesNotThrow(() -> baselineAdvisories(layout).check(imported(NAMED_SETTER)));
	}

	@Test
	void setter_off_mode_disables_the_rule() {
		BaselineLayout layout = BaselineLayout.builder(NAMED_SETTER)
				.setterMode(RuleMode.OFF)
				.build();

		assertDoesNotThrow(() -> baseline(layout).check(imported(NAMED_SETTER)));
		assertDoesNotThrow(() -> baselineAdvisories(layout).check(imported(NAMED_SETTER)));
	}

	@Test
	void setter_rule_ignores_private_and_static_setters() {
		String message = failureOf(baselineAdvisories(NAMED_SETTER), NAMED_SETTER);

		assertFalse(message.contains("setSecret"));
		assertFalse(message.contains("setGlobalDefault"));
	}

	@Test
	void setter_rule_detects_fluent_setters_writing_the_same_named_field() {
		String message = failureOf(baselineAdvisories(FLUENT_SETTER), FLUENT_SETTER);

		assertTrue(message.contains(FLUENT_SETTER
				+ ".model.Account.name(java.lang.String) is a fluent setter of field name"));
		assertFalse(message.contains("Account.name() "));
	}

	@Test
	void setter_rule_allows_behavior_methods_getters_and_constructors() {
		BaselineLayout layout = BaselineLayout.builder(BEHAVIOR)
				.setterMode(RuleMode.FAIL)
				.build();

		assertDoesNotThrow(() -> baseline(layout).check(imported(BEHAVIOR)));
	}

	@Test
	void setter_rule_allows_overrides_of_methods_declared_outside_the_base_package() {
		BaselineLayout layout = BaselineLayout.builder(FRAMEWORK_OVERRIDE)
				.setterMode(RuleMode.FAIL)
				.build();

		assertDoesNotThrow(() -> baseline(layout).check(
				new ClassFileImporter().importPackages(
						FRAMEWORK_OVERRIDE, "org.springframework.beans.factory")));
	}

	@Test
	void setter_rule_allows_overrides_of_generic_methods_declared_outside_the_base_package() {
		String basePackage = FIXTURES + ".setter.generic";
		BaselineLayout layout = BaselineLayout.builder(basePackage)
				.setterMode(RuleMode.FAIL)
				.build();

		assertDoesNotThrow(() -> baseline(layout).check(imported(basePackage)));
	}

	@Test
	void setter_rule_reports_in_base_contracts_once_at_their_declaration() {
		String basePackage = FIXTURES + ".setter.inbase";

		String message = failureOf(baselineAdvisories(basePackage), basePackage);

		assertTrue(message.contains(basePackage + ".model.Named.setName(java.lang.String) is a setter"));
		assertFalse(message.contains("NamedAccount.setName"));
	}

	@Test
	void fluent_setters_implementing_abstract_fluent_methods_are_reported() {
		String basePackage = FIXTURES + ".setter.fluentcontract";

		String message = failureOf(baselineAdvisories(basePackage), basePackage);

		assertTrue(message.contains(basePackage
				+ ".model.FluentAccount.name(java.lang.String) is a fluent setter of field name"));
	}

	@Test
	void package_private_methods_in_other_packages_are_not_overridden() {
		String basePackage = FIXTURES + ".setter.crosspackage";

		String message = failureOf(baselineAdvisories(basePackage), basePackage);

		assertTrue(message.contains(basePackage + ".ext.Sub.setLevel(int) is a setter"));
	}

	@Test
	void setters_are_reported_when_missing_dependencies_are_not_resolved() {
		ArchConfiguration.withThreadLocalScope(configuration -> {
			configuration.setResolveMissingDependenciesFromClassPath(false);

			String message = failureOf(baselineAdvisories(NAMED_SETTER), NAMED_SETTER);

			assertTrue(message.contains("Account.setName(java.lang.String) is a setter"));
		});
	}

	@Test
	void inherited_jdk_methods_are_recognized_when_missing_dependencies_are_not_resolved() {
		String basePackage = FIXTURES + ".setter.jdk";
		ArchConfiguration.withThreadLocalScope(configuration -> {
			configuration.setResolveMissingDependenciesFromClassPath(false);

			String message = failureOf(baselineAdvisories(basePackage), basePackage);

			assertTrue(message.contains("LogHandler.setOwn(java.lang.String) is a setter"));
			assertFalse(message.contains("setFormatter"));
		});
	}

	@Test
	void setter_exceptions_accept_exact_class_names_and_package_patterns() {
		BaselineLayout byClass = BaselineLayout.builder(NAMED_SETTER)
				.setterMode(RuleMode.FAIL)
				.setterExceptions("{base}.model.Account")
				.build();
		BaselineLayout byPackage = BaselineLayout.builder(NAMED_SETTER)
				.setterMode(RuleMode.FAIL)
				.setterExceptions(FIXTURES + ".setter..")
				.build();

		assertDoesNotThrow(() -> baseline(byClass).check(imported(NAMED_SETTER)));
		assertDoesNotThrow(() -> baseline(byPackage).check(imported(NAMED_SETTER)));
		assertDoesNotThrow(() -> baseline(BaselineLayout.builder(FLUENT_SETTER)
				.setterMode(RuleMode.FAIL)
				.setterExceptions("{base}..*Account")
				.build()).check(imported(FLUENT_SETTER)));
		assertEquals(List.of(NAMED_SETTER + ".model.Account"), byClass.setterExceptions());
	}

	@Test
	void static_non_final_fields_fail_by_default() {
		String message = failureOf(baseline(STATIC_FIELD), STATIC_FIELD);

		assertTrue(message.contains(STATIC_FIELD + ".model.Counter.count is a static non-final field"));
	}

	@Test
	void static_field_rule_allows_constants_and_enums() {
		assertDoesNotThrow(() -> baseline(CONSTANTS).check(imported(CONSTANTS)));
	}

	@Test
	void static_field_rule_can_be_advisory_or_off() {
		BaselineLayout advisory = BaselineLayout.builder(STATIC_FIELD)
				.staticFieldMode(RuleMode.ADVISORY)
				.build();
		BaselineLayout off = BaselineLayout.builder(STATIC_FIELD)
				.staticFieldMode(RuleMode.OFF)
				.build();
		BaselineLayout excepted = BaselineLayout.builder(STATIC_FIELD)
				.staticFieldExceptions("{base}..")
				.build();

		assertDoesNotThrow(() -> baseline(advisory).check(imported(STATIC_FIELD)));
		assertTrue(failureOf(baselineAdvisories(advisory), STATIC_FIELD)
				.contains("Counter.count is a static non-final field"));
		assertDoesNotThrow(() -> baseline(off).check(imported(STATIC_FIELD)));
		assertDoesNotThrow(() -> baselineAdvisories(off).check(imported(STATIC_FIELD)));
		assertDoesNotThrow(() -> baseline(excepted).check(imported(STATIC_FIELD)));
	}

	@Test
	void domain_oriented_rejects_default_methods_in_domain_and_application() {
		String message = failureOf(domainOriented(INVALID_DEFAULTS), INVALID_DEFAULTS);

		assertTrue(message.contains(INVALID_DEFAULTS
				+ ".application.OrderLookup.findOrUnknown(java.lang.String) is a default interface method"));
		assertTrue(message.contains(INVALID_DEFAULTS
				+ ".domain.Pricing.withTax() is a default interface method"));
	}

	@Test
	void default_method_rule_allows_sealed_functional_static_and_outer_layer_interfaces() {
		assertDoesNotThrow(() -> domainOriented(VALID_DEFAULTS).check(imported(VALID_DEFAULTS)));
	}

	@Test
	void baseline_does_not_check_default_methods() {
		assertDoesNotThrow(() -> baseline(INVALID_DEFAULTS).check(imported(INVALID_DEFAULTS)));
	}

	@Test
	void hexagonal_rejects_default_methods_on_configured_ports() {
		HexagonalLayout layout = HexagonalLayout.builder(PORT_DEFAULTS)
				.inboundPortPackages()
				.outboundPortPackages("{base}.contract..")
				.build();

		String message = failureOf(hexagonal(layout), PORT_DEFAULTS);
		assertTrue(message.contains("LoadOrderPort.loadOrEmpty(java.lang.String)"
				+ " is a default interface method"));
	}

	@Test
	void default_method_rule_supports_modes_and_exceptions() {
		DomainOrientedLayout advisory = DomainOrientedLayout.builder(INVALID_DEFAULTS)
				.defaultMethodMode(RuleMode.ADVISORY)
				.build();
		DomainOrientedLayout excepted = DomainOrientedLayout.builder(INVALID_DEFAULTS)
				.defaultMethodExceptions("{base}.domain..", "{base}.application.OrderLookup")
				.build();
		DomainOrientedLayout off = DomainOrientedLayout.builder(INVALID_DEFAULTS)
				.defaultMethodMode(RuleMode.OFF)
				.build();

		assertDoesNotThrow(() -> domainOriented(advisory).check(imported(INVALID_DEFAULTS)));
		assertTrue(failureOf(domainOrientedAdvisories(advisory), INVALID_DEFAULTS)
				.contains("is a default interface method"));
		assertDoesNotThrow(() -> domainOriented(excepted).check(imported(INVALID_DEFAULTS)));
		assertDoesNotThrow(() -> domainOriented(off).check(imported(INVALID_DEFAULTS)));
	}

	@Test
	void higher_tier_advisories_include_lower_tier_advisories() {
		assertThrows(AssertionError.class,
				() -> domainOrientedAdvisories(NAMED_SETTER).check(imported(NAMED_SETTER)));
		assertThrows(AssertionError.class,
				() -> hexagonalAdvisories(NAMED_SETTER).check(imported(NAMED_SETTER)));
	}

	@Test
	void rule_modes_and_exceptions_survive_to_builder_and_tier_promotion() {
		BaselineLayout baselineLayout = BaselineLayout.builder("com.acme.orders")
				.setterMode(RuleMode.FAIL)
				.setterExceptions("{base}.legacy..")
				.staticFieldMode(RuleMode.ADVISORY)
				.staticFieldExceptions("{base}.Registry")
				.build();
		DomainOrientedLayout domainLayout = DomainOrientedLayout.builder(baselineLayout)
				.defaultMethodMode(RuleMode.ADVISORY)
				.defaultMethodExceptions("{base}.domain.Legacy")
				.build();
		HexagonalLayout hexagonalLayout = HexagonalLayout.of(domainLayout);

		BaselineLayout copy = baselineLayout.toBuilder().build();
		assertEquals(RuleMode.FAIL, copy.setterMode());
		assertEquals(List.of("com.acme.orders.legacy.."), copy.setterExceptions());
		assertEquals(RuleMode.ADVISORY, copy.staticFieldMode());
		assertEquals(List.of("com.acme.orders.Registry"), copy.staticFieldExceptions());
		assertEquals(RuleMode.FAIL, domainLayout.setterMode());
		assertEquals(RuleMode.ADVISORY, domainLayout.toBuilder().build().defaultMethodMode());
		assertEquals(RuleMode.ADVISORY, hexagonalLayout.defaultMethodMode());
		assertEquals(
				List.of("com.acme.orders.domain.Legacy"),
				hexagonalLayout.toBuilder().build().defaultMethodExceptions());
		assertEquals(RuleMode.ADVISORY, hexagonalLayout.staticFieldMode());
	}

	@Test
	void default_rule_modes_match_the_documented_policy() {
		HexagonalLayout layout = HexagonalLayout.of("com.acme.orders");

		assertEquals(RuleMode.ADVISORY, layout.setterMode());
		assertEquals(RuleMode.FAIL, layout.staticFieldMode());
		assertEquals(RuleMode.FAIL, layout.defaultMethodMode());
	}

	@Test
	void rule_modes_must_not_be_null() {
		assertThrows(NullPointerException.class,
				() -> BaselineLayout.builder("com.acme.orders").setterMode(null));
		assertThrows(NullPointerException.class,
				() -> DomainOrientedLayout.builder("com.acme.orders").defaultMethodMode(null));
	}

	private static JavaClasses imported(String packageName) {
		return new ClassFileImporter().importPackages(packageName);
	}

	private static String failureOf(ArchRule rule, String packageName) {
		AssertionError failure = assertThrows(
				AssertionError.class,
				() -> rule.check(imported(packageName)));
		return failure.getMessage();
	}
}
