package io.github.psm8.archunit.fixtures.transaction.invalidplacement.infrastructure;

import org.springframework.transaction.annotation.Transactional;

public final class MethodTransactionalAdapter {
	@Transactional
	public void execute() {
	}
}
