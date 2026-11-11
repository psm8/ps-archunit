package io.github.psm8.archunit.fixtures.sharedlib.valid.adapter.in;

import io.github.psm8.archunit.fixtures.sharedlib.valid.application.port.in.ListItemsUseCase;
import io.github.psm8.archunit.fixtures.sharedlib.valid.lib.RequestTag;

final class ItemControllerAdapter {
	private final ListItemsUseCase useCase;

	ItemControllerAdapter(ListItemsUseCase useCase) {
		this.useCase = useCase;
	}

	String handle() {
		return RequestTag.of("in") + useCase.list().size();
	}
}
