package io.github.psm8.archunit.fixtures.vertical.messaging.adapter.in;

import io.github.psm8.archunit.fixtures.vertical.messaging.application.port.in.PlaceOrderUseCase;
import io.github.psm8.archunit.fixtures.vertical.messaging.domain.Order;

final class PlaceOrderAdapter implements PlaceOrderUseCase {
	private final PlaceOrderUseCase useCase;

	PlaceOrderAdapter(PlaceOrderUseCase useCase) {
		this.useCase = useCase;
	}

	@Override
	public Order place(Order order) {
		return useCase.place(order);
	}
}
