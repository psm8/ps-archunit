package io.github.psm8.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static io.github.psm8.archunit.HexagonalArchitectureRules.minimal;
import static io.github.psm8.archunit.HexagonalArchitectureRules.standard;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HexagonalArchitectureRulesTest {
    @Test
    void minimal_accepts_valid_hexagonal_layout() {
        // Skill: clean-ddd-hexagonal/SKILL.md, Dependency Rule.
        minimal("io.github.psm8.archunit.fixtures.valid")
                .check(imported("io.github.psm8.archunit.fixtures.valid"));
    }

    @Test
    void standard_accepts_valid_hexagonal_layout() {
        // Skills: clean-ddd-hexagonal/references/HEXAGONAL.md, port and adapter contracts;
        // clean-ddd-hexagonal/references/TESTING.md, architecture tests.
        standard("io.github.psm8.archunit.fixtures.valid")
                .check(imported("io.github.psm8.archunit.fixtures.valid"));
    }

    @Test
    void minimal_allows_missing_optional_layers() {
        // Skill: clean-ddd-hexagonal/SKILL.md, start simple and evolve complexity.
        assertDoesNotThrow(() -> minimal("io.github.psm8.archunit.fixtures.partial")
                .check(imported("io.github.psm8.archunit.fixtures.partial")));
    }

    @Test
    void standard_rejects_outward_application_dependency() {
        // Skill: clean-ddd-hexagonal/SKILL.md, Dependency Rule.
        assertRuleFails(
                standard("io.github.psm8.archunit.fixtures.invalid.onion"),
                "io.github.psm8.archunit.fixtures.invalid.onion");
    }

    @Test
    void standard_rejects_nested_package_cycle() {
        // Skill: clean-ddd-hexagonal/references/TESTING.md, architecture tests;
        // TDD skill: tests verify behavior through a public seam.
        assertRuleFails(
                standard("io.github.psm8.archunit.fixtures.invalid.cycle"),
                "io.github.psm8.archunit.fixtures.invalid.cycle");
    }

    @Test
    void standard_rejects_framework_dependencies_in_core() {
        // Skills: clean-ddd-hexagonal/references/HEXAGONAL.md, no framework types in ports;
        // clean-ddd-hexagonal/SKILL.md, domain has zero external dependencies.
        assertRuleFails(
                standard("io.github.psm8.archunit.fixtures.invalid.framework"),
                "io.github.psm8.archunit.fixtures.invalid.framework");
    }

    @Test
    void standard_rejects_non_interface_port_class() {
        // Skill: clean-ddd-hexagonal/references/HEXAGONAL.md, ports are interfaces.
        assertRuleFails(
                standard("io.github.psm8.archunit.fixtures.invalid.portclass"),
                "io.github.psm8.archunit.fixtures.invalid.portclass");
    }

    @Test
    void standard_rejects_wrong_port_names() {
        // Skill: clean-ddd-hexagonal/references/HEXAGONAL.md, driving/driven port roles;
        // project convention, port suffix naming.
        assertRuleFails(
                standard("io.github.psm8.archunit.fixtures.invalid.naming"),
                "io.github.psm8.archunit.fixtures.invalid.naming");
    }

    @Test
    void standard_rejects_unwired_outbound_adapter() {
        // Skills: clean-ddd-hexagonal/references/HEXAGONAL.md, adapters implement ports;
        // clean-ddd-hexagonal/SKILL.md, dependency-inversion seams.
        assertRuleFails(
                standard("io.github.psm8.archunit.fixtures.invalid.adapter"),
                "io.github.psm8.archunit.fixtures.invalid.adapter");
    }

    @Test
    void factories_reject_invalid_base_packages() {
        // Skill: tdd/SKILL.md, validate the public seam and its input contract.
        assertThrows(IllegalArgumentException.class, () -> minimal(null));
        assertThrows(IllegalArgumentException.class, () -> standard(""));
        assertThrows(IllegalArgumentException.class, () -> standard("com.acme.orders."));
        assertThrows(IllegalArgumentException.class, () -> standard("com.acme.orders..bad"));
        assertThrows(IllegalArgumentException.class, () -> standard("com.class"));
        assertDoesNotThrow(() -> minimal("com.\u00e9quipe"));
    }

    private static JavaClasses imported(String packageName) {
        return new ClassFileImporter().importPackages(packageName);
    }

    private static void assertRuleFails(com.tngtech.archunit.lang.ArchRule rule, String packageName) {
        assertThrows(AssertionError.class, () -> rule.check(imported(packageName)));
    }
}
