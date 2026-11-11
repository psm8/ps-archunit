package io.github.psm8.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static io.github.psm8.archunit.BaselineArchitectureRules.baseline;
import static io.github.psm8.archunit.BaselineArchitectureRules.baselineAdvisories;
import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOriented;
import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOrientedAdvisories;
import static io.github.psm8.archunit.HexagonalArchitectureRules.hexagonal;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanCreationAndBuilderRulesTest {
	private static final String FIXTURES = "io.github.psm8.archunit.fixtures.switchable";
	private static final String SCANNED = FIXTURES + ".beancreation.scanned";
	private static final String EXPLICIT = FIXTURES + ".beancreation.explicit";
	private static final String GENERATED_BUILDER = FIXTURES + ".builder.generated";
	private static final String SHAPED_BUILDER = FIXTURES + ".builder.shaped";
	private static final String NAMED_BUILDER = FIXTURES + ".builder.named";

	@Test
	void bean_creation_default_rises_with_the_level() {
		assertEquals(RuleMode.OFF, BaselineLayout.of("com.acme").beanCreationMode());
		assertEquals(RuleMode.ADVISORY, DomainOrientedLayout.of("com.acme").beanCreationMode());
		assertEquals(RuleMode.FAIL, HexagonalLayout.of("com.acme").beanCreationMode());
	}

	@Test
	void configured_bean_creation_mode_survives_level_promotion() {
		BaselineLayout baseline = BaselineLayout.builder("com.acme")
				.beanCreationMode(RuleMode.ADVISORY)
				.build();

		assertEquals(RuleMode.ADVISORY, HexagonalLayout.of(baseline).beanCreationMode());
		assertEquals(
				RuleMode.FAIL,
				HexagonalLayout.of(DomainOrientedLayout.of("com.acme")).beanCreationMode());
		assertEquals(
				RuleMode.ADVISORY,
				HexagonalLayout.of(baseline).toBuilder().build().beanCreationMode());
	}

	@Test
	void baseline_does_not_check_bean_creation_by_default() {
		assertDoesNotThrow(() -> baseline(SCANNED).check(imported(SCANNED)));
		assertDoesNotThrow(() -> baselineAdvisories(SCANNED).check(imported(SCANNED)));
	}

	@Test
	void domain_oriented_reports_component_scanning_as_advisories() {
		DomainOrientedLayout layout = DomainOrientedLayout.builder(SCANNED)
				.infrastructurePackages("{base}..")
				.build();

		String message = failure(domainOrientedAdvisories(layout), SCANNED);

		assertTrue(message.contains(SCANNED + ".app.OrderService is annotated with @Service;"
				+ " register it with a @Bean method in a @Configuration class"), message);
		assertTrue(message.contains(SCANNED + ".app.OrderMapper is annotated with @Component;"
				+ " register it with a @Bean method in a @Configuration class"), message);
		assertTrue(message.contains(SCANNED + ".app.ScanningConfiguration enables component"
				+ " scanning; register beans with @Bean methods or @Import"), message);
		assertTrue(message.contains(SCANNED + ".app.OrdersApplication enables component scanning"
				+ " through @SpringBootApplication; use @SpringBootConfiguration with"
				+ " @EnableAutoConfiguration"), message);
	}

	@Test
	void hexagonal_fails_on_component_scanning_by_default() {
		HexagonalLayout layout = HexagonalLayout.builder(SCANNED)
				.infrastructurePackages("{base}..")
				.build();

		String message = failure(hexagonal(layout), SCANNED);

		assertTrue(message.contains(SCANNED + ".app.OrderService is annotated with @Service"), message);
	}

	@Test
	void bean_creation_exceptions_exempt_listed_classes() {
		HexagonalLayout layout = HexagonalLayout.builder(SCANNED)
				.infrastructurePackages("{base}..")
				.beanCreationExceptions("{base}.app.OrdersApplication")
				.build();

		String message = failure(hexagonal(layout), SCANNED);

		assertFalse(message.contains("OrdersApplication"), message);
		assertTrue(message.contains("OrderService"), message);
	}

	@Test
	void explicit_beans_and_own_stereotypes_pass_and_bean_methods_need_no_direct_configuration() {
		HexagonalLayout layout = HexagonalLayout.builder(EXPLICIT)
				.infrastructurePackages("{base}..")
				.build();

		assertDoesNotThrow(() -> hexagonal(layout).check(imported(EXPLICIT)));
	}

	@Test
	void generated_builders_are_reported_once_and_not_as_setters() {
		String message = failure(baselineAdvisories(GENERATED_BUILDER), GENERATED_BUILDER);

		assertTrue(message.contains(GENERATED_BUILDER + ".model.Order$OrderBuilder is a builder;"
				+ " prefer a constructor or factory method"), message);
		assertFalse(message.contains("fluent setter"), message);
		assertEquals(1, message.split("is a builder", -1).length - 1, message);
	}

	@Test
	void builders_are_recognised_by_shape_without_the_name() {
		String message = failure(baselineAdvisories(SHAPED_BUILDER), SHAPED_BUILDER);

		assertTrue(message.contains(SHAPED_BUILDER + ".model.InvoiceAssembler is a builder"),
				message);
		assertFalse(message.contains("fluent setter"), message);
	}

	@Test
	void builder_named_classes_are_reported_and_keep_their_setter_findings() {
		String message = failure(baselineAdvisories(NAMED_BUILDER), NAMED_BUILDER);

		assertTrue(message.contains(NAMED_BUILDER + ".model.ResponseBuilder is a builder"),
				message);
		assertTrue(message.contains("ResponseBuilder.setPrefix(java.lang.String) is a setter"),
				message);
	}

	@Test
	void builder_mode_fail_and_exceptions() {
		BaselineLayout failing = BaselineLayout.builder(GENERATED_BUILDER)
				.builderMode(RuleMode.FAIL)
				.build();
		BaselineLayout excepted = BaselineLayout.builder(GENERATED_BUILDER)
				.builderMode(RuleMode.FAIL)
				.builderExceptions("{base}.model.Order$OrderBuilder")
				.build();

		assertTrue(failure(baseline(failing), GENERATED_BUILDER).contains("is a builder"));
		assertDoesNotThrow(() -> baseline(excepted).check(imported(GENERATED_BUILDER)));
	}

	@Test
	void builder_mode_off_reports_nothing_and_still_skips_builder_setters() {
		BaselineLayout layout = BaselineLayout.builder(GENERATED_BUILDER)
				.builderMode(RuleMode.OFF)
				.build();

		assertDoesNotThrow(() -> baselineAdvisories(layout).check(imported(GENERATED_BUILDER)));
	}

	private static String failure(ArchRule rule, String packageName) {
		return assertThrows(AssertionError.class, () -> rule.check(imported(packageName)))
				.getMessage();
	}

	private static JavaClasses imported(String packageName) {
		return new ClassFileImporter().importPackages(packageName);
	}
}
