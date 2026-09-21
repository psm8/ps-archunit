package io.github.psm8.archunit.fixtures.invalid.direction.infrastructure.infrastructure;

import io.github.psm8.archunit.fixtures.invalid.direction.infrastructure.api.ApiDependency;

public final class BadInfrastructure {
	private final ApiDependency dependency;

	public BadInfrastructure(ApiDependency dependency) {
		this.dependency = dependency;
	}

	public ApiDependency dependency() {
		return dependency;
	}
}
