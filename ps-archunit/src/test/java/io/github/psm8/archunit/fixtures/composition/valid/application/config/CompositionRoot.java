package io.github.psm8.archunit.fixtures.composition.valid.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.fake.FrameworkType;
import io.github.psm8.archunit.fixtures.composition.valid.adapter.out.ExternalOrderGateway;

@Configuration(proxyBeanMethods = false)
final class CompositionRoot {
	@Bean
	FrameworkType frameworkType() {
		return new FrameworkType();
	}

	@Bean
	ExternalOrderGateway externalOrderGateway() {
		return new ExternalOrderGateway();
	}
}
