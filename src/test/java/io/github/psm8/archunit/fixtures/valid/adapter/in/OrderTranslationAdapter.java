package io.github.psm8.archunit.fixtures.valid.adapter.in;

import io.github.psm8.archunit.fixtures.valid.domain.model.Order;

final class OrderTranslationAdapter {
	Order translate(String id) {
		return new Order(id);
	}
}
