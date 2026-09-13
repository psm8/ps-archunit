package io.github.psm8.archunit.fixtures.valid.application.port.in;

import io.github.psm8.archunit.fixtures.valid.domain.model.Order;

public interface PlaceOrderUseCase {
    Order execute(String orderId);
}
