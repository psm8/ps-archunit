package io.github.psm8.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOriented;
import static io.github.psm8.archunit.HexagonalArchitectureRules.hexagonal;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedLibPackagesTest {
	private static final String FIXTURES = "io.github.psm8.archunit.fixtures.sharedlib";
	private static final String VALID = FIXTURES + ".valid";
	private static final String CORE_USE = FIXTURES + ".coreuse";
	private static final String NOT_LEAF = FIXTURES + ".notleaf";

	@Test
	void shared_lib_defaults_to_lib_packages() {
		DomainOrientedLayout domainOriented = DomainOrientedLayout.of("com.acme.orders");
		HexagonalLayout hexagonal = HexagonalLayout.of("com.acme.orders");

		List<String> expected = List.of("com.acme.orders.lib..", "com.acme.orders.*.lib..");
		assertEquals(expected, domainOriented.sharedLibPackages());
		assertEquals(expected, hexagonal.sharedLibPackages());
	}

	@Test
	void shared_lib_packages_are_replaced_or_appended_with_base_macro() {
		HexagonalLayout replaced = HexagonalLayout.builder("com.acme.orders")
				.sharedLibPackages("{base}.observability..")
				.build();
		DomainOrientedLayout appended = DomainOrientedLayout.builder("com.acme.orders")
				.addSharedLibPackages("{base}.observability..")
				.build();

		assertEquals(List.of("com.acme.orders.observability.."), replaced.sharedLibPackages());
		assertEquals(
				List.of(
						"com.acme.orders.lib..",
						"com.acme.orders.*.lib..",
						"com.acme.orders.observability.."),
				appended.sharedLibPackages());
		assertEquals(
				replaced.sharedLibPackages(),
				replaced.toBuilder().build().sharedLibPackages());
	}

	@Test
	void hexagonal_lets_inbound_and_outbound_adapters_use_shared_lib() {
		assertDoesNotThrow(() -> hexagonal(VALID).check(imported(VALID)));
	}

	@Test
	void domain_oriented_lets_api_and_infrastructure_use_shared_lib() {
		DomainOrientedLayout layout = DomainOrientedLayout.builder(VALID)
				.apiPackages("{base}.adapter.in..")
				.infrastructurePackages("{base}.adapter.out..")
				.build();

		assertDoesNotThrow(() -> domainOriented(layout).check(imported(VALID)));
	}

	@Test
	void shared_lib_classes_are_unclassified_without_the_group() {
		HexagonalLayout layout = HexagonalLayout.builder(VALID)
				.sharedLibPackages()
				.build();

		String message = failure(hexagonal(layout), VALID);

		assertTrue(message.contains(VALID + ".lib.RequestTag does not belong"), message);
	}

	@Test
	void domain_and_application_may_not_depend_on_shared_lib() {
		String message = failure(hexagonal(CORE_USE), CORE_USE);

		assertTrue(message.startsWith(
				"Architecture Violation [Priority: MEDIUM] - Rule 'the hexagonal architecture under "
						+ CORE_USE), message);
		assertTrue(message.contains("<" + CORE_USE + ".domain.model.Item.tagged()> calls method <"
				+ CORE_USE + ".lib.RequestTag.of"), message);
		assertTrue(message.contains("<" + CORE_USE + ".application.service.ItemService.tag()>"
				+ " calls method <" + CORE_USE + ".lib.RequestTag.of"), message);
		assertTrue(message.contains(portUsesSharedLib()), message);
	}

	@Test
	void domain_oriented_rejects_application_and_port_dependencies_on_shared_lib() {
		String message = failure(domainOriented(CORE_USE), CORE_USE);

		assertTrue(message.contains("<" + CORE_USE + ".application.service.ItemService.tag()>"
				+ " calls method <" + CORE_USE + ".lib.RequestTag.of"), message);
		assertTrue(message.contains(portUsesSharedLib()), message);
	}

	@Test
	void shared_lib_depends_on_nothing_else_in_the_base_package() {
		String message = failure(hexagonal(NOT_LEAF), NOT_LEAF);

		assertTrue(message.contains(formatterCallsItem()), message);
		assertTrue(message.contains("calls method <" + NOT_LEAF + ".adapter.in.ItemView.render()>"),
				message);
	}

	@Test
	void domain_oriented_keeps_shared_lib_a_leaf() {
		DomainOrientedLayout layout = DomainOrientedLayout.builder(NOT_LEAF)
				.apiPackages("{base}.adapter.in..")
				.build();

		String message = failure(domainOriented(layout), NOT_LEAF);

		assertTrue(message.contains(formatterCallsItem()), message);
	}

	@Test
	void dependency_direction_ignores_apply_to_shared_lib() {
		HexagonalLayout layout = HexagonalLayout.builder(NOT_LEAF)
				.ignoreDependency("{base}.lib.ItemFormatter", "{base}.domain.model.Item")
				.ignoreDependency("{base}.lib.ItemFormatter", "{base}.adapter.in.ItemView")
				.build();

		assertDoesNotThrow(() -> hexagonal(layout).check(imported(NOT_LEAF)));
	}

	private static String portUsesSharedLib() {
		return "<" + CORE_USE + ".application.port.out.ItemTagPort.tag(" + CORE_USE
				+ ".lib.RequestTag)> has parameter of type <" + CORE_USE + ".lib.RequestTag>";
	}

	private static String formatterCallsItem() {
		return "ItemFormatter.format(" + NOT_LEAF + ".domain.model.Item, " + NOT_LEAF
				+ ".adapter.in.ItemView)> calls method <" + NOT_LEAF + ".domain.model.Item.id()>";
	}

	private static String failure(ArchRule rule, String packageName) {
		AssertionError error = assertThrows(
				AssertionError.class, () -> rule.check(imported(packageName)));
		return error.getMessage();
	}

	private static JavaClasses imported(String packageName) {
		return new ClassFileImporter().importPackages(packageName);
	}
}
