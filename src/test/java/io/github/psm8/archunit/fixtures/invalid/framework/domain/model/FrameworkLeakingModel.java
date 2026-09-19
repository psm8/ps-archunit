package io.github.psm8.archunit.fixtures.invalid.framework.domain.model;

import org.springframework.fake.FrameworkType;

public final class FrameworkLeakingModel {
	private final FrameworkType frameworkType;

	public FrameworkLeakingModel(FrameworkType frameworkType) {
		this.frameworkType = frameworkType;
	}

	public FrameworkType frameworkType() {
		return frameworkType;
	}
}
