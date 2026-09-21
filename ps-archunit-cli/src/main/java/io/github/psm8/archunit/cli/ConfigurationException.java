package io.github.psm8.archunit.cli;

final class ConfigurationException extends IllegalArgumentException {
	ConfigurationException(String message) {
		super(message);
	}

	ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
