package io.github.psm8.archunit.fixtures.vertical.messaging.application;

import io.github.psm8.archunit.fixtures.vertical.messaging.application.port.out.OrderPort;
import io.github.psm8.archunit.fixtures.vertical.messaging.domain.Order;

public final class PlaceOrderService {
	private final OrderPort orderPort;

	public PlaceOrderService(OrderPort orderPort) {
		this.orderPort = orderPort;
	}

	public Order place(Order order) {
		orderPort.save(order);
		return order;
	}
}
