package io.github.psm8.archunit.fixtures.vertical.messaging.application.port.in;

import io.github.psm8.archunit.fixtures.vertical.messaging.domain.Order;

public interface PlaceOrderUseCase {
	Order place(Order order);
}
