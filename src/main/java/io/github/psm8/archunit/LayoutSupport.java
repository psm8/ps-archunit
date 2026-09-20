package io.github.psm8.archunit;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;

final class LayoutSupport {
	private LayoutSupport() {
	}

	static String defaultText(String value, String fallback) {
		return value == null ? fallback : value;
	}

	static String optionalText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		if (!value.equals(value.trim())) {
			throw new IllegalArgumentException("option value must not be padded: " + value);
		}
		return value;
	}

	static void validateBasePackage(String value) {
		if (value == null
				|| !value.equals(value.trim())
				|| value.isBlank()
				|| !SourceVersion.isName(value)) {
			throw new IllegalArgumentException(
					"basePackage must be a concrete Java package name: " + value);
		}
	}

	static String requiredPattern(String value) {
		if (value == null || value.isBlank() || !value.equals(value.trim())
				|| value.contains(" ")) {
			throw new IllegalArgumentException(
					"package pattern must not contain whitespace: " + value);
		}
		return value;
	}

	static String requiredTypeName(String value) {
		if (value == null
				|| value.isBlank()
				|| !value.equals(value.trim())
				|| !SourceVersion.isName(value)
				|| value.indexOf('.') <= 0) {
			throw new IllegalArgumentException(
					"application interface bean type must be an exact Java type name: " + value);
		}
		return value;
	}

	static void replacePatterns(List<String> target, String... values) {
		target.clear();
		addPatterns(target, values);
	}

	static void replaceTypeNames(List<String> target, String... values) {
		target.clear();
		if (values == null) {
			return;
		}
		for (String value : values) {
			target.add(requiredTypeName(value));
		}
	}

	static void addPatterns(List<String> target, String... values) {
		if (values == null) {
			return;
		}
		for (String value : values) {
			if (value == null) {
				continue;
			}
			String pattern = value.isBlank() ? "" : requiredPattern(value);
			if (!pattern.isEmpty()) {
				target.add(pattern);
			}
		}
	}

	static void prependPatterns(List<String> target, String... values) {
		List<String> existing = new ArrayList<>(target);
		target.clear();
		addPatterns(target, values);
		target.addAll(existing);
	}

	static List<String> copyRequiredPatterns(List<String> values, String label) {
		if (values == null) {
			throw new NullPointerException(label + "s");
		}
		List<String> copy = new ArrayList<>();
		for (String value : values) {
			copy.add(requiredPattern(value));
		}
		return List.copyOf(copy);
	}
}
