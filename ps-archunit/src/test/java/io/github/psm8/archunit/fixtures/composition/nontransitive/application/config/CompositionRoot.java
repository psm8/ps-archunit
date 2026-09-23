package io.github.psm8.archunit.fixtures.composition.nontransitive.application.config;

import io.github.psm8.archunit.fixtures.composition.nontransitive.application.service.FrameworkLeakingService;
import org.springframework.context.annotation.Configuration;
import org.springframework.fake.FrameworkType;

@Configuration(proxyBeanMethods = false)
public final class CompositionRoot {
	private final FrameworkLeakingService service;

	public CompositionRoot() {
		service = new FrameworkLeakingService(new FrameworkType());
	}

	public FrameworkLeakingService service() {
		return service;
	}
}
