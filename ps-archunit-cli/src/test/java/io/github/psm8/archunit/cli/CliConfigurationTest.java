package io.github.psm8.archunit.cli;

import io.github.psm8.archunit.BaselineLayout;
import io.github.psm8.archunit.ConfigurationVisibility;
import io.github.psm8.archunit.HexagonalLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CliConfigurationTest {
	@Test
	void parses_versioned_hexagonal_configuration_with_replace_and_append_sections() {
		String yaml = """
				schemaVersion: 1
				tier: hexagonal
				basePackage: com.acme.orders
				hexagonal:
				  adapterMode: lax
				replace:
				  outputs: ["{base}.*.api.."]
				  domain: ["{base}.domain.."]
				  applicationPackages: ["{base}.application.."]
				  apiPackages: ["{base}.api.."]
				  infrastructurePackages: ["{base}.infrastructure.."]
				  inboundAdapterPackages: []
				  outboundAdapterPackages: []
				append:
				  mixedAdapterPackages: ["{base}.adapter.."]
				  portSignatureExceptions: ["{base}.application.port.in.LegacyUseCase"]
				""";

		HexagonalLayout layout = (HexagonalLayout) new ConfigurationParser().parse(yaml).layout();

		assertEquals(List.of("com.acme.orders.*.api.."), layout.outputs());
		assertEquals(
				List.of(
						"com.acme.orders.adapter..",
						"com.acme.orders.*.adapter..",
						"com.acme.orders.adapter.."),
				layout.mixedAdapterPackages());
		assertEquals(
				List.of("com.acme.orders.application.port.in.LegacyUseCase"),
				layout.portSignatureExceptions());
		assertEquals(List.of(), layout.inboundAdapterPackages());
		assertEquals(List.of(), layout.outboundAdapterPackages());
	}

	@Test
	void rejects_unknown_configuration_keys_before_layout_creation() {
		String yaml = """
				schemaVersion: 1
				tier: baseline
				basePackage: com.acme.orders
				replace:
				  notARealBuilderOption: ["value"]
				""";

		assertThrows(ConfigurationException.class, () -> new ConfigurationParser().parse(yaml));
	}

	@Test
	void configuration_visibility_defaults_to_unrestricted() {
		String yaml = """
				schemaVersion: 1
				tier: baseline
				basePackage: com.acme.orders
				""";

		BaselineLayout layout = (BaselineLayout) new ConfigurationParser().parse(yaml).layout();

		assertEquals(ConfigurationVisibility.UNRESTRICTED, layout.configurationVisibility());
	}

	@Test
	void parses_package_private_configuration_visibility_under_replace() {
		String yaml = """
				schemaVersion: 1
				tier: baseline
				basePackage: com.acme.orders
				replace:
				  configurationVisibility: packagePrivate
				""";

		BaselineLayout layout = (BaselineLayout) new ConfigurationParser().parse(yaml).layout();

		assertEquals(ConfigurationVisibility.PACKAGE_PRIVATE, layout.configurationVisibility());
	}

	@Test
	void preserves_configuration_class_allowlist_with_package_private_visibility() {
		String yaml = """
				schemaVersion: 1
				tier: baseline
				basePackage: com.acme.orders
				replace:
				  configurationVisibility: packagePrivate
				  publicConfigurationClasses: ["com.acme.orders.config.PublicConfiguration"]
				""";

		BaselineLayout layout = (BaselineLayout) new ConfigurationParser().parse(yaml).layout();

		assertEquals(ConfigurationVisibility.PACKAGE_PRIVATE, layout.configurationVisibility());
		assertEquals(
				List.of("com.acme.orders.config.PublicConfiguration"),
				layout.publicConfigurationClasses());
	}

	@Test
	void rejects_invalid_configuration_visibility_value() {
		String yaml = """
				schemaVersion: 1
				tier: baseline
				basePackage: com.acme.orders
				replace:
				  configurationVisibility: public
				""";

		assertThrows(ConfigurationException.class, () -> new ConfigurationParser().parse(yaml));
	}

	@Test
	void rejects_configuration_visibility_in_append() {
		String yaml = """
				schemaVersion: 1
				tier: baseline
				basePackage: com.acme.orders
				append:
				  configurationVisibility: packagePrivate
				""";

		assertThrows(ConfigurationException.class, () -> new ConfigurationParser().parse(yaml));
	}
}
