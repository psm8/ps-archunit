package io.github.psm8.archunit.cli;

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
}
