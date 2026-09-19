package io.github.psm8.archunit.fixtures.invalid.direction.domain.domain;

import io.github.psm8.archunit.fixtures.invalid.direction.domain.application.ApplicationDependency;

public final class BadDomain {
	private final ApplicationDependency dependency;

	public BadDomain(ApplicationDependency dependency) {
		this.dependency = dependency;
	}

	public ApplicationDependency dependency() {
		return dependency;
	}
}
