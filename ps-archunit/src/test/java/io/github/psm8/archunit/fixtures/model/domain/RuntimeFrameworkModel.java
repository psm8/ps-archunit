package io.github.psm8.archunit.fixtures.model.domain;

import io.github.psm8.archunit.fixtures.support.model.framework.RuntimeFrameworkType;

public final class RuntimeFrameworkModel {
	private final RuntimeFrameworkType frameworkType;

	public RuntimeFrameworkModel(RuntimeFrameworkType frameworkType) {
		this.frameworkType = frameworkType;
	}

	public RuntimeFrameworkType frameworkType() {
		return frameworkType;
	}
}
