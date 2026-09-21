package io.github.psm8.archunit.fixtures.beanexposure.allowlist.allowed.application.config;

import io.github.psm8.archunit.fixtures.beanexposure.allowlist.allowed.application.AllowedBeanContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
final class BeanExposureConfiguration {
	@Bean
	AllowedBeanContract allowedContract() {
		return null;
	}
}
