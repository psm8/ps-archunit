package io.github.psm8.archunit.fixtures.valid.application.port.out;

import io.github.psm8.archunit.fixtures.valid.domain.model.Order;

import java.util.Optional;

public interface OrderRepositoryPort {
    Optional<Order> findById(String id);
}
