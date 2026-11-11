package io.github.psm8.archunit.fixtures.sharedlib.coreuse.lib;

public final class RequestTag {
	private RequestTag() {
	}

	public static String of(String direction) {
		return direction;
	}
}
