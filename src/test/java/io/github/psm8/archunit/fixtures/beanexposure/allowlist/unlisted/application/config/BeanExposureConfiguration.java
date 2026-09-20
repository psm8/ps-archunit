package io.github.psm8.archunit.fixtures.beanexposure.allowlist.unlisted.application.config;

import io.github.psm8.archunit.fixtures.beanexposure.allowlist.unlisted.application.UnlistedBeanContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
final class BeanExposureConfiguration {
	@Bean
	UnlistedBeanContract unlistedContract() {
		return null;
	}
}
