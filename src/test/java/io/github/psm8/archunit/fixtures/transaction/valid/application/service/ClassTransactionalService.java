package io.github.psm8.archunit.fixtures.transaction.valid.application.service;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public final class ClassTransactionalService {
	public void execute() {
	}
}
