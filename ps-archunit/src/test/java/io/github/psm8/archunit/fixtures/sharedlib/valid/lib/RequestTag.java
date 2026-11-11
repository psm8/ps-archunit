package io.github.psm8.archunit.fixtures.sharedlib.valid.lib;

import org.springframework.fake.FrameworkType;

import java.util.UUID;

public final class RequestTag {
	private RequestTag() {
	}

	public static String of(String direction) {
		return direction + ":" + UUID.randomUUID();
	}

	public static String describe(FrameworkType source) {
		return String.valueOf(source);
	}
}
