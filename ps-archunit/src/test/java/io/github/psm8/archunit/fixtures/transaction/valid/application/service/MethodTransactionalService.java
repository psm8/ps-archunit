package io.github.psm8.archunit.fixtures.transaction.valid.application.service;

import org.springframework.transaction.annotation.Transactional;

public final class MethodTransactionalService {
	@Transactional
	public void execute() {
	}
}
