package io.github.psm8.archunit.fixtures.sharedlib.valid.lib;

import io.github.psm8.archunit.fixtures.sharedlib.valid.application.service.ListItemsService;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class SharedLibConfiguration {
	String wiredService() {
		return ListItemsService.class.getName();
	}
}
