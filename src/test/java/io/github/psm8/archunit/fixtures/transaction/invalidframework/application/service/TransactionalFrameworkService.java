package io.github.psm8.archunit.fixtures.transaction.invalidframework.application.service;

import org.springframework.fake.FrameworkType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public final class TransactionalFrameworkService {
	private final FrameworkType frameworkType;

	public TransactionalFrameworkService(FrameworkType frameworkType) {
		this.frameworkType = frameworkType;
	}

	public FrameworkType frameworkType() {
		return frameworkType;
	}
}
