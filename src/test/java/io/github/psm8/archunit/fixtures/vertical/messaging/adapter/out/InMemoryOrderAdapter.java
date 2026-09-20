package io.github.psm8.archunit.fixtures.vertical.messaging.adapter.out;

import io.github.psm8.archunit.fixtures.vertical.messaging.application.port.out.OrderPort;
import io.github.psm8.archunit.fixtures.vertical.messaging.domain.Order;

final class InMemoryOrderAdapter implements OrderPort {
	@Override
	public void save(Order order) {
	}
}
