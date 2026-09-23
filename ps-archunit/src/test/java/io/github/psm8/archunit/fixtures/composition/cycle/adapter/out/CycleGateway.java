package io.github.psm8.archunit.fixtures.composition.cycle.adapter.out;

import io.github.psm8.archunit.fixtures.composition.cycle.application.config.CycleCompositionRoot;

public final class CycleGateway {
	private final CycleCompositionRoot root;

	public CycleGateway(CycleCompositionRoot root) {
		this.root = root;
	}

	public CycleCompositionRoot root() {
		return root;
	}
}
