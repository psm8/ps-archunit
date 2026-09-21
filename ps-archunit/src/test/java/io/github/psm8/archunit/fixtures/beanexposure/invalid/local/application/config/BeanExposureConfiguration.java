package io.github.psm8.archunit.fixtures.beanexposure.invalid.local.application.config;

import io.github.psm8.archunit.fixtures.beanexposure.invalid.local.application.LocalBeanContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
final class BeanExposureConfiguration {
	@Bean
	LocalBeanContract localContract() {
		return null;
	}
}
