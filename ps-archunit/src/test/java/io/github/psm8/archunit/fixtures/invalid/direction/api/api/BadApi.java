package io.github.psm8.archunit.fixtures.invalid.direction.api.api;

import io.github.psm8.archunit.fixtures.invalid.direction.api.infrastructure.InfrastructureDependency;

public final class BadApi {
	private final InfrastructureDependency dependency;

	public BadApi(InfrastructureDependency dependency) {
		this.dependency = dependency;
	}

	public InfrastructureDependency dependency() {
		return dependency;
	}
}
