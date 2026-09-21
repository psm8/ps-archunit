package io.github.psm8.archunit.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class Version {
	private static final String GROUP = "io.github.psm8";
	private static final String ARTIFACT = "ps-archunit-cli";

	private Version() {
	}

	static String current() {
		String implementationVersion = Version.class.getPackage().getImplementationVersion();
		if (implementationVersion != null && !implementationVersion.isBlank()) {
			return implementationVersion;
		}
		String resource = "META-INF/maven/" + GROUP + "/" + ARTIFACT + "/pom.properties";
		try (InputStream stream = Version.class.getClassLoader().getResourceAsStream(resource)) {
			if (stream == null) {
				return "unknown";
			}
			Properties properties = new Properties();
			properties.load(stream);
			return properties.getProperty("version", "unknown");
		} catch (IOException exception) {
			return "unknown";
		}
	}
}
