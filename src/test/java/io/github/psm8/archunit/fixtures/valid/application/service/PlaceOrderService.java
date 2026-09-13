package io.github.psm8.archunit.fixtures.valid.application.service;

import io.github.psm8.archunit.fixtures.valid.application.port.in.PlaceOrderUseCase;
import io.github.psm8.archunit.fixtures.valid.application.port.out.OrderRepositoryPort;
import io.github.psm8.archunit.fixtures.valid.domain.model.Order;

public final class PlaceOrderService implements PlaceOrderUseCase {
    private final OrderRepositoryPort repository;

    public PlaceOrderService(OrderRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Order execute(String orderId) {
        return repository.findById(orderId).orElseThrow();
    }
}
