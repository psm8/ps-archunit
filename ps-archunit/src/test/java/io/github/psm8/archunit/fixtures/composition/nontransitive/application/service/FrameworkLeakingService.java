package io.github.psm8.archunit.fixtures.composition.nontransitive.application.service;

import org.springframework.fake.FrameworkType;

public final class FrameworkLeakingService {
	private final FrameworkType frameworkType;

	public FrameworkLeakingService(FrameworkType frameworkType) {
		this.frameworkType = frameworkType;
	}

	public FrameworkType frameworkType() {
		return frameworkType;
	}
}
