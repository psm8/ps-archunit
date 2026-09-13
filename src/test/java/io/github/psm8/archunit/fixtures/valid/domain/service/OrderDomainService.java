package io.github.psm8.archunit.fixtures.valid.domain.service;

import io.github.psm8.archunit.fixtures.valid.domain.model.Order;

public final class OrderDomainService {
    public boolean canProcess(Order order) {
        return order.id() != null;
    }
}
