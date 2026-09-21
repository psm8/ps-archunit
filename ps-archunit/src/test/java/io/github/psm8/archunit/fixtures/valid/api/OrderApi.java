package io.github.psm8.archunit.fixtures.valid.api;

import io.github.psm8.archunit.fixtures.valid.domain.model.Order;

final class OrderApi {
	private final Order order;

	OrderApi(Order order) {
		this.order = order;
	}

	Order order() {
		return order;
	}
}
