package io.github.psm8.archunit.fixtures.invalid.direction.inbound.adapter.in;

import io.github.psm8.archunit.fixtures.invalid.direction.inbound.infrastructure.InfrastructureDependency;

final class BadInboundAdapter {
	private final InfrastructureDependency dependency;

	BadInboundAdapter(InfrastructureDependency dependency) {
		this.dependency = dependency;
	}

	InfrastructureDependency dependency() {
		return dependency;
	}
}
