package io.github.psm8.archunit.cli;

enum Tier {
	BASELINE("baseline"),
	DOMAIN_ORIENTED("domainOriented"),
	HEXAGONAL("hexagonal");

	private final String yamlName;

	Tier(String yamlName) {
		this.yamlName = yamlName;
	}

	static Tier parse(Object value) {
		for (Tier tier : values()) {
			if (tier.yamlName.equals(value)) {
				return tier;
			}
		}
		throw new ConfigurationException(
				"tier must be baseline, domainOriented, or hexagonal");
	}

	String yamlName() {
		return yamlName;
	}
}
