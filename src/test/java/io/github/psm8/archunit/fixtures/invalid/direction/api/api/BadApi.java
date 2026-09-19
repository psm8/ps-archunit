package io.github.psm8.archunit.fixtures.invalid.direction.api.api;

import io.github.psm8.archunit.fixtures.invalid.direction.api.domain.DomainDependency;

public final class BadApi {
	private final DomainDependency dependency;

	public BadApi(DomainDependency dependency) {
		this.dependency = dependency;
	}

	public DomainDependency dependency() {
		return dependency;
	}
}
