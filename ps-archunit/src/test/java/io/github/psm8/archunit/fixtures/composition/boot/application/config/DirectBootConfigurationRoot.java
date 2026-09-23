package io.github.psm8.archunit.fixtures.composition.boot.application.config;

import io.github.psm8.archunit.fixtures.composition.boot.adapter.out.BootGateway;
import org.springframework.boot.SpringBootConfiguration;

@SpringBootConfiguration
public final class DirectBootConfigurationRoot {
	private final BootGateway gateway;

	public DirectBootConfigurationRoot(BootGateway gateway) {
		this.gateway = gateway;
	}

	public BootGateway gateway() {
		return gateway;
	}
}
