package io.github.psm8.archunit.fixtures.invalid.parameterannotation.application.port.in;

import org.springframework.fake.FrameworkParameter;

public interface FrameworkAnnotatedUseCase {
	void execute(@FrameworkParameter String value);
}
