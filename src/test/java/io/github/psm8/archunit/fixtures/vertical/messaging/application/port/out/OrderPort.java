package io.github.psm8.archunit.fixtures.vertical.messaging.application.port.out;

import io.github.psm8.archunit.fixtures.vertical.messaging.domain.Order;

public interface OrderPort {
	void save(Order order);
}
