package io.github.psm8.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.psm8.archunit.BaselineArchitectureRules.baseline;
import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOriented;
import static io.github.psm8.archunit.HexagonalArchitectureRules.hexagonal;
import static io.github.psm8.archunit.HexagonalArchitectureRules.laxHexagonal;
import static io.github.psm8.archunit.HexagonalArchitectureRules.strictHexagonal;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureRuleTiersTest {
	@Test
	void baseline_accepts_valid_application_layout() {
		baseline("io.github.psm8.archunit.fixtures.valid")
				.check(imported("io.github.psm8.archunit.fixtures.valid"));
	}

	@Test
	void domain_oriented_accepts_valid_application_layout() {
		domainOriented("io.github.psm8.archunit.fixtures.valid")
				.check(imported("io.github.psm8.archunit.fixtures.valid"));
	}

	@Test
	void baseline_does_not_enforce_domain_direction() {
		assertDoesNotThrow(() -> baseline(
				"io.github.psm8.archunit.fixtures.invalid.onion")
				.check(imported("io.github.psm8.archunit.fixtures.invalid.onion")));
	}

	@Test
	void domain_oriented_adds_application_direction() {
		String basePackage = "io.github.psm8.archunit.fixtures.invalid.onion";
		assertRuleFails(
				domainOriented(DomainOrientedLayout.builder(basePackage)
						.infrastructurePackages(basePackage + ".adapter.out..")
						.build()),
				basePackage);
	}

	@Test
	void baseline_does_not_enforce_framework_isolation() {
		assertDoesNotThrow(() -> baseline(
				"io.github.psm8.archunit.fixtures.invalid.framework")
				.check(imported("io.github.psm8.archunit.fixtures.invalid.framework")));
	}

	@Test
	void domain_oriented_rejects_non_model_framework_dependencies() {
		assertRuleFails(
				domainOriented(DomainOrientedLayout.of(
						"io.github.psm8.archunit.fixtures.invalid.framework")),
				"io.github.psm8.archunit.fixtures.invalid.framework");
	}

	@Test
	void domain_oriented_rejects_domain_dependencies_on_application() {
		String basePackage = "io.github.psm8.archunit.fixtures.invalid.direction.domain";

		assertRuleFails(domainOriented(basePackage), basePackage);
	}

	@Test
	void domain_oriented_rejects_application_dependencies_on_api() {
		String basePackage = "io.github.psm8.archunit.fixtures.invalid.direction.application";

		assertRuleFails(domainOriented(basePackage), basePackage);
	}

	@Test
	void domain_oriented_allows_api_dependencies_on_domain() {
		String basePackage = "io.github.psm8.archunit.fixtures.valid";

		assertDoesNotThrow(() -> domainOriented(basePackage)
				.check(imported(basePackage)));
	}

	@Test
	void hexagonal_allows_inbound_adapter_dependencies_on_domain() {
		String basePackage = "io.github.psm8.archunit.fixtures.valid";

		assertDoesNotThrow(() -> hexagonal(basePackage)
				.check(imported(basePackage)));
	}

	@Test
	void domain_oriented_rejects_api_dependencies_on_infrastructure() {
		String basePackage = "io.github.psm8.archunit.fixtures.invalid.direction.api";

		assertRuleFails(domainOriented(basePackage), basePackage);
	}

	@Test
	void hexagonal_rejects_inbound_adapter_dependencies_on_infrastructure() {
		String basePackage = "io.github.psm8.archunit.fixtures.invalid.direction.inbound";

		assertRuleFails(hexagonal(basePackage), basePackage);
	}

	@Test
	void domain_oriented_rejects_infrastructure_dependencies_on_api() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.direction.infrastructure";

		assertRuleFails(domainOriented(basePackage), basePackage);
	}

	@Test
	void domain_oriented_allows_configured_model_annotations() {
		String basePackage = "io.github.psm8.archunit.fixtures.model";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.domain(basePackage + ".domain..")
				.domainModelFrameworkPackages(basePackage + ".framework..")
				.build();

		domainOriented(layout).check(new ClassFileImporter().importClasses(
				io.github.psm8.archunit.fixtures.model.domain.AnnotatedModel.class,
				io.github.psm8.archunit.fixtures.model.framework.ModelAnnotation.class));
	}

	@Test
	void hexagonal_preserves_configured_model_annotation_allowance() {
		String basePackage = "io.github.psm8.archunit.fixtures.model";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.domain(basePackage + ".domain..")
				.domainModelFrameworkPackages(basePackage + ".framework..")
				.build();

		hexagonal(layout).check(new ClassFileImporter().importClasses(
				io.github.psm8.archunit.fixtures.model.domain.AnnotatedModel.class,
				io.github.psm8.archunit.fixtures.model.framework.ModelAnnotation.class));
	}

	@Test
	void domain_oriented_rejects_runtime_types_from_configured_model_namespace() {
		String basePackage = "io.github.psm8.archunit.fixtures.model";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.domain(basePackage + ".domain..")
				.domainModelFrameworkPackages(basePackage + ".framework..")
				.build();

		assertThrows(
				AssertionError.class,
				() -> domainOriented(layout).check(new ClassFileImporter().importClasses(
						io.github.psm8.archunit.fixtures.model.domain.RuntimeFrameworkModel.class,
						io.github.psm8.archunit.fixtures.model.framework.RuntimeFrameworkType.class)));
	}

	@Test
	void hexagonal_allows_framework_dependencies_in_configuration_composition_roots() {
		String basePackage = "io.github.psm8.archunit.fixtures.composition.valid";

		hexagonal(basePackage).check(imported(basePackage));
	}

	@Test
	void baseline_discovers_configuration_from_annotations_without_package_selectors() {
		String basePackage = "io.github.psm8.archunit.fixtures.composition.valid";

		baseline(basePackage).check(imported(basePackage));
	}

	@Test
	void hexagonal_does_not_extend_composition_root_exception_to_application_services() {
		String basePackage = "io.github.psm8.archunit.fixtures.composition.nontransitive";

		assertRuleFails(hexagonal(basePackage), basePackage);
	}

	@Test
	void strict_profile_accepts_valid_hexagonal_layout() {
		strictHexagonal("io.github.psm8.archunit.fixtures.valid")
				.check(imported("io.github.psm8.archunit.fixtures.valid"));
	}

	@Test
	void strict_and_lax_factories_are_not_deprecated() throws NoSuchMethodException {
		assertFalse(HexagonalArchitectureRules.class
				.getMethod("strictHexagonal", String.class)
				.isAnnotationPresent(Deprecated.class));
		assertFalse(HexagonalArchitectureRules.class
				.getMethod("laxHexagonal", String.class)
				.isAnnotationPresent(Deprecated.class));
	}

	@Test
	void custom_profile_accepts_valid_hexagonal_layout() {
		hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.valid"))
				.check(imported("io.github.psm8.archunit.fixtures.valid"));
	}

	@Test
	void custom_profile_accepts_sealed_boundary_types_in_port_packages() {
		hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.valid.sealed"))
				.check(imported("io.github.psm8.archunit.fixtures.valid.sealed"));
	}

	@Test
	void lax_profile_allows_missing_optional_layers() {
		assertDoesNotThrow(() -> laxHexagonal("io.github.psm8.archunit.fixtures.partial")
				.check(imported("io.github.psm8.archunit.fixtures.partial")));
	}

	@Test
	void custom_profile_rejects_outward_application_dependency() {
		assertRuleFails(
				hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.invalid.onion")),
				"io.github.psm8.archunit.fixtures.invalid.onion");
	}

	@Test
	void custom_profile_rejects_nested_package_cycle() {
		assertRuleFails(
				hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.invalid.cycle")),
				"io.github.psm8.archunit.fixtures.invalid.cycle");
	}

	@Test
	void custom_profile_rejects_framework_dependencies_in_core() {
		assertRuleFails(
				hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.invalid.framework")),
				"io.github.psm8.archunit.fixtures.invalid.framework");
	}

	@Test
	void custom_profile_rejects_non_interface_port_class() {
		assertRuleFails(
				hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.invalid.portclass")),
				"io.github.psm8.archunit.fixtures.invalid.portclass");
	}

	@Test
	void custom_profile_rejects_wrong_port_names() {
		assertRuleFails(
				hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.invalid.naming")),
				"io.github.psm8.archunit.fixtures.invalid.naming");
	}

	@Test
	void custom_profile_rejects_unwired_outbound_adapter() {
		assertRuleFails(
				hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.invalid.adapter")),
				"io.github.psm8.archunit.fixtures.invalid.adapter");
	}

	@Test
	void factories_reject_invalid_base_packages() {
		assertThrows(IllegalArgumentException.class, () -> strictHexagonal(null));
		assertThrows(IllegalArgumentException.class, () -> strictHexagonal(""));
		assertThrows(IllegalArgumentException.class, () -> strictHexagonal("com.acme.orders."));
		assertThrows(
				IllegalArgumentException.class,
				() -> strictHexagonal("com.acme.orders..bad"));
		assertThrows(IllegalArgumentException.class, () -> strictHexagonal("com.class"));
		assertDoesNotThrow(() -> strictHexagonal("com.\u00e9quipe"));
	}

	@Test
	void hexagonal_layout_uses_feature_oriented_defaults() {
		HexagonalLayout layout = HexagonalLayout.of("com.acme.orders");

		assertEquals(List.of("com.acme.orders.domain.."), layout.domain());
		assertEquals(
				List.of("com.acme.orders.application.."),
				layout.applicationPackages());
		assertEquals(
				List.of("com.acme.orders.adapter.in.."),
				layout.inboundAdapterPackages());
		assertEquals(
				List.of("com.acme.orders.adapter.out.."),
				layout.outboundAdapterPackages());
		assertEquals(List.of(), layout.mixedAdapterPackages());
		assertEquals(
				List.of("com.acme.orders.api.."),
				layout.apiPackages());
		assertEquals(
				List.of(
						"com.acme.orders.infrastructure.."),
				layout.infrastructurePackages());
		assertEquals(
				List.of(
						"jakarta.persistence..",
						"jakarta.validation..",
						"javax.persistence..",
						"javax.validation..",
						"com.fasterxml.jackson.annotation.."),
				layout.domainModelFrameworkPackages());
	}

	@Test
	void baseline_api_does_not_expose_higher_tier_selectors() {
		assertThrows(
				NoSuchMethodException.class,
				() -> BaselineLayout.class.getMethod("domain"));
		assertThrows(
				NoSuchMethodException.class,
				() -> BaselineLayout.class.getMethod("apiPackages"));
		assertThrows(
				NoSuchMethodException.class,
				() -> BaselineLayout.class.getMethod("inboundAdapterPackages"));
		assertThrows(
				NoSuchMethodException.class,
				() -> BaselineLayout.class.getMethod("dependencyDirectionIgnores"));
		assertThrows(
				NoSuchMethodException.class,
				() -> BaselineLayout.Builder.class.getMethod(
						"ignoreDependency", String.class, String.class));
	}

	@Test
	void hexagonal_api_selector_is_configured_before_promotion() {
		String basePackage = "com.acme.orders";
		DomainOrientedLayout domain = DomainOrientedLayout.builder(basePackage)
				.apiPackages(basePackage + ".http..")
				.build();

		HexagonalLayout layout = HexagonalLayout.builder(domain).build();

		assertEquals(List.of(basePackage + ".http.."), layout.apiPackages());
	}

	@Test
	void hexagonal_layout_hides_domain_oriented_api_surface() {
		assertThrows(
				NoSuchMethodException.class,
				() -> HexagonalLayout.class.getMethod("domainOriented"));
		assertThrows(
				NoSuchMethodException.class,
				() -> HexagonalLayout.class.getMethod("apiPackages"));
		assertThrows(
				NoSuchMethodException.class,
				() -> HexagonalLayout.Builder.class.getMethod(
						"apiPackages", String[].class));
		assertThrows(
				NoSuchMethodException.class,
				() -> HexagonalLayout.Builder.class.getMethod(
						"addApiPackages", String[].class));
	}

	@Test
	void domain_oriented_defaults_exclude_adapter_groups() {
		DomainOrientedLayout layout = DomainOrientedLayout.of("com.acme.orders");

		assertEquals(List.of("com.acme.orders.api.."), layout.apiPackages());
		assertEquals(
				List.of("com.acme.orders.infrastructure.."),
				layout.infrastructurePackages());
	}

	@Test
	void framework_dependency_selector_replaces_defaults_but_keeps_model_coverage() {
		DomainOrientedLayout layout = DomainOrientedLayout.builder("com.acme.orders")
				.frameworkDependencyPackages()
				.build();

		assertFalse(layout.frameworkDependencyPackages().contains("org.springframework.."));
		assertTrue(layout.frameworkDependencyPackages()
				.contains("jakarta.persistence.."));
	}

	@Test
	void promotion_copies_lower_snapshot_without_mutating_it() {
		BaselineLayout baseline = BaselineLayout.builder("com.acme.orders")
				.outputs("com.acme.orders.result..")
				.build();

		DomainOrientedLayout promoted = DomainOrientedLayout.builder(baseline)
				.apiPackages("com.acme.orders.publicapi..")
				.build();

		assertEquals(List.of("com.acme.orders.result.."), baseline.outputs());
		assertEquals(List.of("com.acme.orders.publicapi.."), promoted.apiPackages());
		assertEquals(List.of("com.acme.orders.result.."), promoted.outputs());
	}

	@Test
	void hexagonal_lower_groups_include_adapters_only_effectively() {
		HexagonalLayout layout = HexagonalLayout.of("com.acme.orders");

		assertEquals(List.of("com.acme.orders.api.."), layout.apiPackages());
		assertEquals(
				List.of("com.acme.orders.api..", "com.acme.orders.adapter.in.."),
				layout.effectiveApiPackages());
		assertEquals(
				List.of(
						"com.acme.orders.infrastructure..",
						"com.acme.orders.adapter.out.."),
				layout.effectiveInfrastructurePackages());
	}

	@Test
	void application_package_configuration_replaces_then_adds_paths() {
		HexagonalLayout layout = HexagonalLayout.builder("com.acme.orders")
				.applicationPackages("com.acme.orders.feature..")
				.addApplicationPackages("com.acme.orders.shared..")
				.build();

		assertEquals(
				List.of("com.acme.orders.feature..", "com.acme.orders.shared.."),
				layout.applicationPackages());
	}

	@Test
	void adapter_package_configuration_is_symmetric() {
		HexagonalLayout layout = HexagonalLayout.builder("com.acme.orders")
				.inboundAdapterPackages("com.acme.orders.http..")
				.outboundAdapterPackages("com.acme.orders.persistence..")
				.build();

		assertEquals(List.of("com.acme.orders.http.."), layout.inboundAdapterPackages());
		assertEquals(
				List.of("com.acme.orders.persistence.."),
				layout.outboundAdapterPackages());
	}

	@Test
	void selectors_replace_with_varargs_and_append_with_canonical_names() {
		DomainOrientedLayout domain = DomainOrientedLayout.builder("com.acme.orders")
				.domain("com.acme.orders.domain.model..", "com.acme.orders.domain.service..")
				.addDomains("com.acme.orders.domain.shared..")
				.apiPackages(
						"com.acme.orders.api..",
						"com.acme.orders.http..")
				.addApiPackages("com.acme.orders.messaging..")
				.infrastructurePackages(
						"com.acme.orders.infrastructure..",
						"com.acme.orders.persistence..")
				.addInfrastructurePackages("com.acme.orders.testinfra..")
				.domainModelFrameworkPackages("jakarta.persistence..")
				.addDomainModelFrameworkPackages("jakarta.validation..")
				.build();
		HexagonalLayout layout = HexagonalLayout.builder(domain)
				.outputs("com.acme.orders.api..", "com.acme.orders.messaging..")
				.addOutputs("com.acme.orders.other..")
				.build();

		assertEquals(
				List.of(
						"com.acme.orders.domain.model..",
						"com.acme.orders.domain.service..",
						"com.acme.orders.domain.shared.."),
				layout.domain());
		assertEquals(
				List.of(
						"com.acme.orders.api..",
						"com.acme.orders.http..",
						"com.acme.orders.messaging.."),
				layout.apiPackages());
		assertEquals(
				List.of(
						"com.acme.orders.infrastructure..",
						"com.acme.orders.persistence..",
						"com.acme.orders.testinfra.."),
				layout.infrastructurePackages());
		assertEquals(
				List.of("jakarta.persistence..", "jakarta.validation.."),
				layout.domainModelFrameworkPackages());
		assertEquals(
				List.of(
						"com.acme.orders.api..",
						"com.acme.orders.messaging..",
						"com.acme.orders.other.."),
				layout.outputs());
	}

	@Test
	void add_only_package_configuration_retains_feature_oriented_defaults() {
		DomainOrientedLayout domain = DomainOrientedLayout.builder("com.acme.orders")
				.addDomains("com.acme.orders.domain.shared..")
				.addApplicationPackages("com.acme.orders.shared..")
				.addApiPackages("com.acme.orders.publicapi..")
				.addInfrastructurePackages("com.acme.orders.database..")
				.addDomainModelFrameworkPackages("com.acme.orders.model.framework..")
				.build();
		HexagonalLayout layout = HexagonalLayout.builder(domain)
				.addInboundPortPackages("com.acme.orders.application.port.in.extra..")
				.addOutboundPortPackages("com.acme.orders.application.port.out.extra..")
				.addInboundAdapterPackages("com.acme.orders.http..")
				.addOutboundAdapterPackages("com.acme.orders.persistence..")
				.addOutputs("com.acme.orders.result..")
				.build();

		assertTrue(layout.domain().contains("com.acme.orders.domain.."));
		assertTrue(layout.domain().contains("com.acme.orders.domain.shared.."));
		assertTrue(layout.applicationPackages().contains("com.acme.orders.application.."));
		assertTrue(layout.applicationPackages().contains("com.acme.orders.shared.."));
		assertTrue(layout.inboundPortPackages()
				.contains("com.acme.orders.application.port.in.."));
		assertTrue(layout.inboundPortPackages()
				.contains("com.acme.orders.application.port.in.extra.."));
		assertTrue(layout.outboundPortPackages()
				.contains("com.acme.orders.application.port.out.."));
		assertTrue(layout.outboundPortPackages()
				.contains("com.acme.orders.application.port.out.extra.."));
		assertTrue(layout.inboundAdapterPackages().contains("com.acme.orders.adapter.in.."));
		assertTrue(layout.inboundAdapterPackages().contains("com.acme.orders.http.."));
		assertTrue(layout.outboundAdapterPackages().contains("com.acme.orders.adapter.out.."));
		assertTrue(layout.outboundAdapterPackages().contains("com.acme.orders.persistence.."));
		assertTrue(layout.apiPackages().contains("com.acme.orders.api.."));
		assertTrue(layout.apiPackages().contains("com.acme.orders.publicapi.."));
		assertTrue(layout.infrastructurePackages()
				.contains("com.acme.orders.infrastructure.."));
		assertTrue(layout.infrastructurePackages().contains("com.acme.orders.database.."));
		assertTrue(layout.domainModelFrameworkPackages()
				.contains("jakarta.persistence.."));
		assertTrue(layout.domainModelFrameworkPackages()
				.contains("com.acme.orders.model.framework.."));
		assertTrue(layout.outputs().contains("com.acme.orders.."));
		assertTrue(layout.outputs().contains("com.acme.orders.result.."));
	}

	@Test
	void layout_exposes_only_explicit_dependency_bans() {
		HexagonalLayout layout = HexagonalLayout.builder("com.acme.orders")
				.dependencyBans(BaselineLayout.DependencyBan.of(
						List.of("com.acme.orders.domain.."),
						List.of("com.acme.orders.legacy..")))
				.build();

		assertEquals(1, layout.dependencyBans().size());
		assertEquals(
				List.of("com.acme.orders.domain.."),
				layout.dependencyBans().get(0).sourcePackages());
	}

	@Test
	void component_annotations_are_not_architecture_selectors() {
		String basePackage = "io.github.psm8.archunit.fixtures.component";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage).build();

		assertDoesNotThrow(() -> hexagonal(layout).check(imported(basePackage)));
	}

	@Test
	void configured_applies_explicit_dependency_bans_with_scoped_exceptions() {
		String basePackage = "io.github.psm8.archunit.fixtures.dependency";
		BaselineLayout.DependencyBan ban = BaselineLayout.DependencyBan
				.of(basePackage + ".source..", basePackage + ".forbidden..")
				.ignoring(
						basePackage + ".source.AllowedDependency",
						basePackage + ".forbidden.ForbiddenType");
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.dependencyBans(ban)
				.build();

		assertDoesNotThrow(() -> hexagonal(layout).check(imported(basePackage)));
	}

	@Test
	void mixed_adapters_are_a_third_onion_layer() {
		String basePackage = "io.github.psm8.archunit.fixtures.mixed";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.mixedAdapterPackages(basePackage + ".adapter.mixed..")
				.build();

		assertDoesNotThrow(() -> hexagonal(layout).check(imported(basePackage)));
	}

	@Test
	void strict_profile_rejects_adapter_implementations_outside_configured_layers() {
		assertRuleFails(
				strictHexagonal("io.github.psm8.archunit.fixtures.invalid.containment"),
				"io.github.psm8.archunit.fixtures.invalid.containment");
	}

	@Test
	void lax_profile_uses_mixed_adapters_without_outbound_port_requirement() {
		assertDoesNotThrow(() -> laxHexagonal("io.github.psm8.archunit.fixtures.mixed.only")
				.check(imported("io.github.psm8.archunit.fixtures.mixed.only")));
	}

	private static JavaClasses imported(String packageName) {
		return new ClassFileImporter().importPackages(packageName);
	}

	private static void assertRuleFails(
			com.tngtech.archunit.lang.ArchRule rule,
			String packageName) {
		assertThrows(AssertionError.class, () -> rule.check(imported(packageName)));
	}
}
