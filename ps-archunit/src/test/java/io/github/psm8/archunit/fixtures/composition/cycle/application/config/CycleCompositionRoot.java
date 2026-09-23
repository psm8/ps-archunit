package io.github.psm8.archunit.fixtures.composition.cycle.application.config;

import io.github.psm8.archunit.fixtures.composition.cycle.adapter.out.CycleGateway;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public final class CycleCompositionRoot {
	private final CycleGateway gateway;

	public CycleCompositionRoot() {
		gateway = new CycleGateway(this);
	}

	public CycleGateway gateway() {
		return gateway;
	}
}
