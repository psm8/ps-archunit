package io.github.psm8.archunit.fixtures.valid.adapter.out;

import io.github.psm8.archunit.fixtures.valid.application.port.out.OrderRepositoryPort;
import io.github.psm8.archunit.fixtures.valid.domain.model.Order;

import java.util.Optional;

public final class InMemoryOrderAdapter implements OrderRepositoryPort {
    @Override
    public Optional<Order> findById(String id) {
        return Optional.of(new Order(id));
    }
}
