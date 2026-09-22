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
	void baseline_accepts_concrete_external_and_functional_interface_bean_returns() {
		String rootPackage = "io.github.psm8.archunit.fixtures.beanexposure.valid";
		String basePackage = rootPackage + ".application";

		assertDoesNotThrow(() -> baseline(basePackage).check(imported(rootPackage)));
	}

	@Test
	void baseline_rejects_unlisted_application_interface_bean_returns() {
		String rootPackage = "io.github.psm8.archunit.fixtures.beanexposure.invalid.local";
		String basePackage = rootPackage + ".application";

		assertBeanExposureRuleFails(
				baseline(basePackage),
				rootPackage,
				basePackage + ".LocalBeanContract");
	}

	@Test
	void baseline_allows_exactly_allowlisted_application_interface_bean_returns() {
		String rootPackage = "io.github.psm8.archunit.fixtures.beanexposure.allowlist.allowed";
		String basePackage = rootPackage + ".application";
		BaselineLayout layout = BaselineLayout.builder(basePackage)
				.allowedApplicationInterfaceBeanTypes(basePackage + ".AllowedBeanContract")
				.build();

		assertDoesNotThrow(() -> baseline(layout).check(imported(rootPackage)));
	}

	@Test
	void baseline_rejects_a_different_application_interface_bean_return() {
		String rootPackage = "io.github.psm8.archunit.fixtures.beanexposure.allowlist.unlisted";
		String basePackage = rootPackage + ".application";
		BaselineLayout layout = BaselineLayout.builder(basePackage)
				.allowedApplicationInterfaceBeanTypes(basePackage + ".SomeOtherContract")
				.build();

		assertBeanExposureRuleFails(
				baseline(layout),
				rootPackage,
				basePackage + ".UnlistedBeanContract");
	}

	@Test
	void bean_interface_allowlist_requires_exact_type_names() {
		assertThrows(
				IllegalArgumentException.class,
				() -> BaselineLayout.builder("com.acme.orders")
						.allowedApplicationInterfaceBeanTypes(""));
		assertThrows(
				IllegalArgumentException.class,
				() -> BaselineLayout.builder("com.acme.orders")
						.allowedApplicationInterfaceBeanTypes("com.acme.orders.."));
		assertThrows(
				IllegalArgumentException.class,
				() -> BaselineLayout.builder("com.acme.orders")
						.allowedApplicationInterfaceBeanTypes("com.acme.orders.*"));
		assertThrows(
				IllegalArgumentException.class,
				() -> BaselineLayout.builder("com.acme.orders")
						.allowedApplicationInterfaceBeanTypes("OrderRepositoryPort"));
		assertThrows(
				IllegalArgumentException.class,
				() -> BaselineLayout.builder("com.acme.orders")
						.allowedApplicationInterfaceBeanTypes(new String[] {null}));
	}

	@Test
	void domain_oriented_accepts_valid_application_layout() {
		String basePackage = "io.github.psm8.archunit.fixtures.valid";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.applicationPackages(
						basePackage + ".application..",
						basePackage + ".sealed.application..")
				.apiPackages(
						basePackage + ".api..",
						basePackage + ".adapter.in..")
				.infrastructurePackages(basePackage + ".adapter.out..")
				.build();

		domainOriented(layout)
				.check(imported("io.github.psm8.archunit.fixtures.valid"));
	}

	@Test
	void baseline_does_not_enforce_domain_direction() {
		assertDoesNotThrow(() -> baseline(
				"io.github.psm8.archunit.fixtures.invalid.onion")
				.check(imported("io.github.psm8.archunit.fixtures.invalid.onion")));
	}

	@Test
	void baseline_allows_unclassified_classes() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.classification";

		assertDoesNotThrow(() -> baseline(basePackage).check(imported(basePackage)));
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

		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.applicationPackages(
						basePackage + ".application..",
						basePackage + ".sealed.application..")
				.apiPackages(
						basePackage + ".api..",
						basePackage + ".adapter.in..")
				.infrastructurePackages(basePackage + ".adapter.out..")
				.build();

		assertDoesNotThrow(() -> domainOriented(layout)
				.check(imported(basePackage)));
	}

	@Test
	void hexagonal_allows_inbound_adapter_dependencies_on_domain() {
		String basePackage = "io.github.psm8.archunit.fixtures.valid";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.applicationPackages(
						basePackage + ".application..",
						basePackage + ".sealed.application..")
				.build();

		assertDoesNotThrow(() -> hexagonal(layout)
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
	void hexagonal_rejects_unclassified_classes() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.classification";

		assertRuleFails(hexagonal(basePackage), basePackage);
	}

	@Test
	void lax_hexagonal_rejects_unclassified_classes() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.classification";

		assertRuleFails(laxHexagonal(basePackage), basePackage);
	}

	@Test
	void domain_oriented_rejects_infrastructure_dependencies_on_api() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.direction.infrastructure";

		assertRuleFails(domainOriented(basePackage), basePackage);
	}

	@Test
	void domain_oriented_rejects_unclassified_classes() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.classification";

		assertRuleFails(domainOriented(basePackage), basePackage);
	}

	@Test
	void overlapping_category_matches_are_accepted() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.classification";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.domain(basePackage + ".domain..")
				.applicationPackages(basePackage + ".domain..")
				.build();

		domainOriented(layout).check(new ClassFileImporter().importClasses(
				io.github.psm8.archunit.fixtures.invalid.classification.domain
						.ClassifiedComponent.class));
	}

	@Test
	void imported_classes_outside_base_package_are_not_classified() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.classification";

		domainOriented(basePackage).check(new ClassFileImporter().importClasses(
				io.github.psm8.archunit.fixtures.invalid.classification.domain
						.ClassifiedComponent.class,
				io.github.psm8.archunit.fixtures.support.ExternalSupport.class));
	}

	@Test
	void empty_level_two_groups_remain_optional() {
		String basePackage = "io.github.psm8.archunit.fixtures.partial";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.apiPackages()
				.infrastructurePackages()
				.build();

		domainOriented(layout).check(imported(basePackage));
	}

	@Test
	void domain_oriented_allows_configured_model_annotations() {
		String basePackage = "io.github.psm8.archunit.fixtures.model";
		String supportPackage =
				"io.github.psm8.archunit.fixtures.support.model.framework";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.domain(basePackage + ".domain..")
				.domainModelFrameworkPackages(supportPackage + "..")
				.build();

		domainOriented(layout).check(new ClassFileImporter().importClasses(
				io.github.psm8.archunit.fixtures.model.domain.AnnotatedModel.class,
				io.github.psm8.archunit.fixtures.support.model.framework.ModelAnnotation.class));
	}

	@Test
	void hexagonal_preserves_configured_model_annotation_allowance() {
		String basePackage = "io.github.psm8.archunit.fixtures.model";
		String supportPackage =
				"io.github.psm8.archunit.fixtures.support.model.framework";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.domain(basePackage + ".domain..")
				.domainModelFrameworkPackages(supportPackage + "..")
				.build();

		hexagonal(layout).check(new ClassFileImporter().importClasses(
				io.github.psm8.archunit.fixtures.model.domain.AnnotatedModel.class,
				io.github.psm8.archunit.fixtures.support.model.framework.ModelAnnotation.class));
	}

	@Test
	void domain_oriented_rejects_runtime_types_from_configured_model_namespace() {
		String basePackage = "io.github.psm8.archunit.fixtures.model";
		String supportPackage =
				"io.github.psm8.archunit.fixtures.support.model.framework";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.domain(basePackage + ".domain..")
				.domainModelFrameworkPackages(supportPackage + "..")
				.build();

		assertThrows(
				AssertionError.class,
				() -> domainOriented(layout).check(new ClassFileImporter().importClasses(
						io.github.psm8.archunit.fixtures.model.domain.RuntimeFrameworkModel.class,
						io.github.psm8.archunit.fixtures.support.model.framework.RuntimeFrameworkType.class)));
	}

	@Test
	void hexagonal_allows_framework_dependencies_in_configuration_composition_roots() {
		String basePackage = "io.github.psm8.archunit.fixtures.composition.valid";

		hexagonal(basePackage).check(imported(basePackage));
	}

	@Test
	void hexagonal_allows_java_se_javax_dependencies_by_default() {
		assertDoesNotThrow(() -> hexagonal("io.github.psm8.archunit.fixtures.valid")
				.check(new ClassFileImporter().importClasses(
						io.github.psm8.archunit.fixtures.valid.application
								.JavaSeDataSourceService.class)));
	}

	@Test
	void baseline_discovers_configuration_from_annotations_without_package_selectors() {
		String basePackage = "io.github.psm8.archunit.fixtures.composition.valid";

		baseline(basePackage).check(imported(basePackage));
	}

	@Test
	void baseline_allows_public_configuration_classes_by_default() {
		assertDoesNotThrow(() -> baseline(
				"io.github.psm8.archunit.fixtures.configurationvisibility")
				.check(new ClassFileImporter().importClasses(
						io.github.psm8.archunit.fixtures.configurationvisibility.config
								.PublicConfiguration.class)));
	}

	@Test
	void package_private_configuration_visibility_rejects_public_configuration_classes() {
		String basePackage = "io.github.psm8.archunit.fixtures.configurationvisibility";
		BaselineLayout layout = BaselineLayout.builder(basePackage)
				.configurationVisibility(ConfigurationVisibility.PACKAGE_PRIVATE)
				.build();

		assertRuleFails(
				baseline(layout),
				io.github.psm8.archunit.fixtures.configurationvisibility.config
						.PublicConfiguration.class);
	}

	@Test
	void package_private_configuration_visibility_allows_exact_configuration_class_exception() {
		String basePackage = "io.github.psm8.archunit.fixtures.configurationvisibility";
		String configurationClass =
				"io.github.psm8.archunit.fixtures.configurationvisibility.config.PublicConfiguration";
		BaselineLayout layout = BaselineLayout.builder(basePackage)
				.configurationVisibility(ConfigurationVisibility.PACKAGE_PRIVATE)
				.publicConfigurationClasses(configurationClass)
				.build();

		assertDoesNotThrow(() -> baseline(layout).check(new ClassFileImporter().importClasses(
				io.github.psm8.archunit.fixtures.configurationvisibility.config
						.PublicConfiguration.class)));
	}

	@Test
	void configuration_properties_visibility_remains_independent_from_configuration_visibility() {
		String basePackage = "io.github.psm8.archunit.fixtures.configurationvisibility";
		Class<?> propertiesClass =
				io.github.psm8.archunit.fixtures.configurationvisibility.config
						.PublicConfigurationProperties.class;

		assertRuleFails(baseline(basePackage), propertiesClass);

		BaselineLayout allowlisted = BaselineLayout.builder(basePackage)
				.publicConfigurationProperties(propertiesClass.getName())
				.build();
		assertDoesNotThrow(() -> baseline(allowlisted).check(
				new ClassFileImporter().importClasses(propertiesClass)));

		BaselineLayout packagePrivateConfiguration = BaselineLayout.builder(basePackage)
				.configurationVisibility(ConfigurationVisibility.PACKAGE_PRIVATE)
				.build();
		assertRuleFails(baseline(packagePrivateConfiguration), propertiesClass);
	}

	@Test
	void configuration_visibility_survives_to_builder_and_tier_promotion() {
		String basePackage = "io.github.psm8.archunit.fixtures.configurationvisibility";
		BaselineLayout baseline = BaselineLayout.builder(basePackage)
				.configurationVisibility(ConfigurationVisibility.PACKAGE_PRIVATE)
				.build();
		BaselineLayout baselineCopy = baseline.toBuilder().build();
		DomainOrientedLayout domain = DomainOrientedLayout.builder(baselineCopy)
				.infrastructurePackages(basePackage + ".config..")
				.build();
		DomainOrientedLayout domainCopy = domain.toBuilder().build();
		HexagonalLayout hexagonal = HexagonalLayout.builder(domainCopy).build();
		HexagonalLayout hexagonalCopy = hexagonal.toBuilder().build();

		assertEquals(ConfigurationVisibility.PACKAGE_PRIVATE, baselineCopy.configurationVisibility());
		assertEquals(ConfigurationVisibility.PACKAGE_PRIVATE, domain.configurationVisibility());
		assertEquals(ConfigurationVisibility.PACKAGE_PRIVATE, domainCopy.configurationVisibility());
		assertEquals(ConfigurationVisibility.PACKAGE_PRIVATE, hexagonal.configurationVisibility());
		assertEquals(
				ConfigurationVisibility.PACKAGE_PRIVATE,
				hexagonalCopy.configurationVisibility());
		assertRuleFails(
				domainOriented(domain),
				io.github.psm8.archunit.fixtures.configurationvisibility.config
						.PublicConfiguration.class);
		assertRuleFails(
				hexagonal(hexagonal),
				io.github.psm8.archunit.fixtures.configurationvisibility.config
						.PublicConfiguration.class);
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
	void strict_profile_accepts_feature_local_vertical_slice() {
		String basePackage = "io.github.psm8.archunit.fixtures.vertical";

		strictHexagonal(basePackage).check(imported(basePackage));
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
		hexagonal(HexagonalLayout.of("io.github.psm8.archunit.fixtures.sealed"))
				.check(imported("io.github.psm8.archunit.fixtures.sealed"));
	}

	@Test
	void custom_port_packages_are_application_core_for_level_three() {
		String basePackage = "io.github.psm8.archunit.fixtures.customports";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.inboundPortPackages(basePackage + ".contract.in..")
				.outboundPortPackages(basePackage + ".contract.out..")
				.build();

		assertDoesNotThrow(() -> hexagonal(layout).check(imported(basePackage)));
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
	void custom_profile_rejects_framework_annotations_on_port_parameters() {
		assertRuleFails(
				hexagonal(HexagonalLayout.of(
						"io.github.psm8.archunit.fixtures.invalid.parameterannotation")),
				"io.github.psm8.archunit.fixtures.invalid.parameterannotation");
	}

	@Test
	void whole_port_signature_exceptions_skip_parameter_annotation_checks() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.parameterannotation.exception";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.portSignatureExceptions(
						basePackage
								+ ".application.port.in.AdapterAnnotatedUseCase")
				.ignoreDependency(
						basePackage + ".application.port.in.AdapterAnnotatedUseCase",
						basePackage + ".adapter.in.AdapterParameter")
				.build();

		assertEquals(
				List.of(basePackage + ".application.port.in.AdapterAnnotatedUseCase"),
				layout.portSignatureExceptions());
		assertDoesNotThrow(() -> hexagonal(layout).check(imported(basePackage)));
	}

	@Test
	void domain_oriented_accepts_configured_transaction_annotations_in_application() {
		String basePackage = "io.github.psm8.archunit.fixtures.transaction.valid";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.transactionAnnotation(
						"org.springframework.transaction.annotation.Transactional")
				.transactionPackages(basePackage + ".application..")
				.build();

		assertDoesNotThrow(() -> domainOriented(layout).check(imported(basePackage)));
	}

	@Test
	void domain_oriented_rejects_transaction_annotations_outside_configured_packages() {
		String basePackage = "io.github.psm8.archunit.fixtures.transaction.invalidplacement";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.transactionAnnotation(
						"org.springframework.transaction.annotation.Transactional")
				.transactionPackages(basePackage + ".application..")
				.build();

		assertRuleFails(
				domainOriented(layout),
				io.github.psm8.archunit.fixtures.transaction.invalidplacement.infrastructure
						.TransactionalAdapter.class);
	}

	@Test
	void domain_oriented_rejects_method_transaction_annotations_outside_configured_packages() {
		String basePackage = "io.github.psm8.archunit.fixtures.transaction.invalidplacement";
		DomainOrientedLayout layout = DomainOrientedLayout.builder(basePackage)
				.transactionAnnotation(
						"org.springframework.transaction.annotation.Transactional")
				.transactionPackages(basePackage + ".application..")
				.build();

		assertRuleFails(
				domainOriented(layout),
				io.github.psm8.archunit.fixtures.transaction.invalidplacement.infrastructure
						.MethodTransactionalAdapter.class);
	}

	@Test
	void transaction_configuration_requires_both_annotation_and_packages() {
		String annotation = "org.springframework.transaction.annotation.Transactional";
		String basePackage = "com.acme.orders";

		assertThrows(
				IllegalArgumentException.class,
				() -> DomainOrientedLayout.builder(basePackage)
						.transactionAnnotation(annotation)
						.build());
		assertThrows(
				IllegalArgumentException.class,
				() -> DomainOrientedLayout.builder(basePackage)
						.transactionPackages(basePackage + ".application..")
						.build());
		assertThrows(
				IllegalArgumentException.class,
				() -> DomainOrientedLayout.builder(basePackage)
						.transactionAnnotation("Transactional"));
		assertThrows(
				IllegalArgumentException.class,
				() -> DomainOrientedLayout.builder(basePackage)
						.transactionAnnotation(
								"org.springframework.transaction.annotation.*"));
	}

	@Test
	void transaction_configuration_is_inherited_by_hexagonal_layouts() {
		String basePackage = "io.github.psm8.archunit.fixtures.transaction.valid";
		String annotation = "org.springframework.transaction.annotation.Transactional";
		DomainOrientedLayout domain = DomainOrientedLayout.builder(basePackage)
				.transactionAnnotation(annotation)
				.transactionPackages("{base}.application..")
				.build();
		HexagonalLayout layout = HexagonalLayout.builder(domain).build();

		assertEquals(annotation, layout.transactionAnnotation());
		assertEquals(
				List.of(basePackage + ".application.."),
				layout.transactionPackages());
		assertDoesNotThrow(() -> hexagonal(layout).check(imported(basePackage)));
	}

	@Test
	void transaction_configuration_survives_domain_layout_promotion() {
		String basePackage = "com.acme.orders";
		String annotation = "org.springframework.transaction.annotation.Transactional";
		DomainOrientedLayout original = DomainOrientedLayout.builder(basePackage)
				.transactionAnnotation(annotation)
				.transactionPackages("{base}.application..")
				.build();

		DomainOrientedLayout copy = original.toBuilder().build();

		assertEquals(annotation, copy.transactionAnnotation());
		assertEquals(
				List.of(basePackage + ".application.."),
				copy.transactionPackages());
	}

	@Test
	void hexagonal_framework_isolation_allows_only_configured_transaction_annotation() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.transaction.invalidframework";
		DomainOrientedLayout domain = DomainOrientedLayout.builder(basePackage)
				.transactionAnnotation(
						"org.springframework.transaction.annotation.Transactional")
				.transactionPackages(basePackage + ".application..")
				.build();

		assertRuleFails(hexagonal(HexagonalLayout.builder(domain).build()), basePackage);
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
	void hexagonal_layout_uses_direct_and_feature_vertical_defaults() {
		HexagonalLayout layout = HexagonalLayout.of("com.acme.orders");

		assertEquals(
				List.of(
						"com.acme.orders.domain..",
						"com.acme.orders.*.domain.."),
				layout.domain());
		assertEquals(
				List.of(
						"com.acme.orders.application..",
						"com.acme.orders.*.application.."),
				layout.applicationPackages());
		assertEquals(
				List.of(
						"com.acme.orders.adapter.in..",
						"com.acme.orders.*.adapter.in.."),
				layout.inboundAdapterPackages());
		assertEquals(
				List.of(
						"com.acme.orders.adapter.out..",
						"com.acme.orders.*.adapter.out.."),
				layout.outboundAdapterPackages());
		assertEquals(List.of(), layout.mixedAdapterPackages());
		assertEquals(
				List.of(
						"com.acme.orders.api..",
						"com.acme.orders.*.api.."),
				layout.apiPackages());
		assertEquals(
				List.of(
						"com.acme.orders.infrastructure..",
						"com.acme.orders.*.infrastructure.."),
				layout.infrastructurePackages());
		assertEquals(
				List.of(
						"com.acme.orders.application.port.in..",
						"com.acme.orders.*.application.port.in.."),
				layout.inboundPortPackages());
		assertEquals(
				List.of(
						"com.acme.orders.application.port.out..",
						"com.acme.orders.*.application.port.out.."),
				layout.outboundPortPackages());
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
	void base_macro_resolves_custom_package_selectors() {
		HexagonalLayout layout = HexagonalLayout.builder("com.acme.orders")
				.domain("{base}.*.domain..")
				.applicationPackages("{base}.*.application..")
				.inboundPortPackages("{base}.*.application.port.in..")
				.outboundPortPackages("{base}.*.application.port.out..")
				.inboundAdapterPackages("{base}.*.adapter.in..")
				.outboundAdapterPackages("{base}.*.adapter.out..")
				.mixedAdapterPackages("{base}.*.adapter..")
				.dependencyBans(BaselineLayout.DependencyBan
						.of("{base}.source..", "{base}.forbidden..")
						.ignoring(
								"{base}.source.Allowed",
								"{base}.forbidden.Forbidden"))
				.outputs("{base}.*.api..")
				.build();

		assertEquals(List.of("com.acme.orders.*.domain.."), layout.domain());
		assertEquals(
				List.of("com.acme.orders.*.application.."),
				layout.applicationPackages());
		assertEquals(
				List.of("com.acme.orders.*.application.port.in.."),
				layout.inboundPortPackages());
		assertEquals(
				List.of("com.acme.orders.*.application.port.out.."),
				layout.outboundPortPackages());
		assertEquals(
				List.of("com.acme.orders.*.adapter.in.."),
				layout.inboundAdapterPackages());
		assertEquals(
				List.of("com.acme.orders.*.adapter.out.."),
				layout.outboundAdapterPackages());
		assertEquals(
				List.of("com.acme.orders.*.adapter.."),
				layout.mixedAdapterPackages());
		assertEquals(
				List.of("com.acme.orders.source.."),
				layout.dependencyBans().get(0).sourcePackages());
		assertEquals(
				List.of("com.acme.orders.forbidden.."),
				layout.dependencyBans().get(0).bannedPackages());
		assertEquals(
				new BaselineLayout.PatternPair(
						"com.acme.orders.source.Allowed",
						"com.acme.orders.forbidden.Forbidden"),
				layout.dependencyBans().get(0).ignoredDependencies().get(0));
		assertEquals(List.of("com.acme.orders.*.api.."), layout.outputs());
	}

	@Test
	void star_matches_exactly_one_vertical_feature_segment() {
		assertTrue(ArchitectureRuleSupport.matches(
				"com.acme.orders.messaging.domain.Order",
				"com.acme.orders.*.domain.."));
		assertFalse(ArchitectureRuleSupport.matches(
				"com.acme.orders.messaging.shipping.domain.Order",
				"com.acme.orders.*.domain.."));
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

		assertEquals(
				List.of(
						"com.acme.orders.api..",
						"com.acme.orders.*.api.."),
				layout.apiPackages());
		assertEquals(
				List.of(
						"com.acme.orders.infrastructure..",
						"com.acme.orders.*.infrastructure.."),
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
	void default_framework_dependencies_exclude_java_se_javax_packages() {
		DomainOrientedLayout layout = DomainOrientedLayout.of("com.acme.orders");

		assertFalse(layout.frameworkDependencyPackages().contains("javax.."));
		assertTrue(layout.frameworkDependencyPackages()
				.contains("javax.persistence.."));
		assertTrue(layout.frameworkDependencyPackages()
				.contains("javax.validation.."));
		assertTrue(layout.frameworkDependencyPackages()
				.contains("javax.servlet.."));
		assertTrue(layout.frameworkDependencyPackages()
				.contains("javax.transaction"));
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

		assertEquals(
				List.of(
						"com.acme.orders.api..",
						"com.acme.orders.*.api.."),
				layout.apiPackages());
		assertEquals(
				List.of(
						"com.acme.orders.application..",
						"com.acme.orders.*.application..",
						"com.acme.orders.application.port.in..",
						"com.acme.orders.*.application.port.in..",
						"com.acme.orders.application.port.out..",
						"com.acme.orders.*.application.port.out.."),
				layout.applicationCorePackages());
		assertEquals(
				List.of(
						"com.acme.orders.api..",
						"com.acme.orders.*.api..",
						"com.acme.orders.adapter.in..",
						"com.acme.orders.*.adapter.in.."),
				layout.effectiveApiPackages());
		assertEquals(
				List.of(
						"com.acme.orders.infrastructure..",
						"com.acme.orders.*.infrastructure..",
						"com.acme.orders.adapter.out..",
						"com.acme.orders.*.adapter.out.."),
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
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.infrastructurePackages(basePackage + ".scanned..")
				.build();

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
				.infrastructurePackages(
						basePackage + ".source..",
						basePackage + ".forbidden..")
				.build();

		assertDoesNotThrow(() -> hexagonal(layout).check(imported(basePackage)));
	}

	@Test
	void mixed_adapters_are_a_third_onion_layer() {
		String basePackage = "io.github.psm8.archunit.fixtures.mixed";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.mixedAdapterPackages(
						basePackage + ".adapter.mixed..",
						basePackage + ".only.adapter..")
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
	void strict_profile_rejects_feature_local_adapters_outside_configured_groups() {
		String basePackage =
				"io.github.psm8.archunit.fixtures.invalid.verticalcontainment";
		HexagonalLayout layout = HexagonalLayout.builder(basePackage)
				.addApplicationPackages(basePackage + ".feature.adapter.other..")
				.build();

		assertRuleFails(hexagonal(layout), basePackage);
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

	private static void assertRuleFails(
			com.tngtech.archunit.lang.ArchRule rule,
			Class<?>... classes) {
		assertThrows(
				AssertionError.class,
				() -> rule.check(new ClassFileImporter().importClasses(classes)));
	}

	private static void assertBeanExposureRuleFails(
			com.tngtech.archunit.lang.ArchRule rule,
			String packageName,
			String expectedReturnType) {
		AssertionError failure = assertThrows(
				AssertionError.class,
				() -> rule.check(imported(packageName)));
		String message = failure.getMessage();
		assertTrue(message != null
				&& message.contains("returns interface " + expectedReturnType));
	}
}
