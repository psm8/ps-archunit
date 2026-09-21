package io.github.psm8.archunit.fixtures.valid.adapter.in;

import io.github.psm8.archunit.fixtures.valid.application.port.in.PlaceOrderUseCase;

final class PlaceOrderControllerAdapter {
	private final PlaceOrderUseCase useCase;

	public PlaceOrderControllerAdapter(PlaceOrderUseCase useCase) {
		this.useCase = useCase;
	}

	public void invoke(String orderId) {
		useCase.execute(orderId);
	}
}
