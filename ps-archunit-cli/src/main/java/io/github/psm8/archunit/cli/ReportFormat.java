package io.github.psm8.archunit.cli;

enum ReportFormat {
	TEXT,
	JSON;

	static ReportFormat parse(String value) {
		try {
			return valueOf(value.toUpperCase(java.util.Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new ConfigurationException("format must be text or json");
		}
	}
}
