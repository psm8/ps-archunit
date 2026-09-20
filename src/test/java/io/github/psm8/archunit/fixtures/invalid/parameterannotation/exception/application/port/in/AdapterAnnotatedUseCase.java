package io.github.psm8.archunit.fixtures.invalid.parameterannotation.exception.application.port.in;

import io.github.psm8.archunit.fixtures.invalid.parameterannotation.exception.adapter.in.AdapterParameter;

public interface AdapterAnnotatedUseCase {
	void execute(@AdapterParameter String value);
}
