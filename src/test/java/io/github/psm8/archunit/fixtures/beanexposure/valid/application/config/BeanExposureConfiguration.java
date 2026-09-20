package io.github.psm8.archunit.fixtures.beanexposure.valid.application.config;

import io.github.psm8.archunit.fixtures.beanexposure.valid.application.ConcreteBean;
import io.github.psm8.archunit.fixtures.beanexposure.valid.application2.ExternalPrefixContract;
import io.github.psm8.archunit.fixtures.beanexposure.valid.external.ExternalContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
final class BeanExposureConfiguration {
	@Bean
	ConcreteBean concreteBean() {
		return new ConcreteBean();
	}

	@Bean
	ExternalContract externalContract() {
		return null;
	}

	@Bean
	ExternalPrefixContract externalPrefixContract() {
		return null;
	}
}
