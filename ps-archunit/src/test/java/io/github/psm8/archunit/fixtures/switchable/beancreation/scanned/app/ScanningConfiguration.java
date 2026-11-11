package io.github.psm8.archunit.fixtures.switchable.beancreation.scanned.app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan
class ScanningConfiguration {
}
