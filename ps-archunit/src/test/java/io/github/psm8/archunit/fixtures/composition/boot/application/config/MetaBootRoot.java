package io.github.psm8.archunit.fixtures.composition.boot.application.config;

import io.github.psm8.archunit.fixtures.composition.boot.adapter.out.BootGateway;

@BootApplicationRoot
public final class MetaBootRoot {
	private final BootGateway gateway;

	public MetaBootRoot(BootGateway gateway) {
		this.gateway = gateway;
	}

	public BootGateway gateway() {
		return gateway;
	}
}
