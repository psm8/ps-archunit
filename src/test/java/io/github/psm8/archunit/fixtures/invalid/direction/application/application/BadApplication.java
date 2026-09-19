package io.github.psm8.archunit.fixtures.invalid.direction.application.application;

import io.github.psm8.archunit.fixtures.invalid.direction.application.api.ApiDependency;

public final class BadApplication {
	private final ApiDependency dependency;

	public BadApplication(ApiDependency dependency) {
		this.dependency = dependency;
	}

	public ApiDependency dependency() {
		return dependency;
	}
}
