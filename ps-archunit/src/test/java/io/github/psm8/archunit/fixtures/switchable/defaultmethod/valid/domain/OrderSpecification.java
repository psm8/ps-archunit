package io.github.psm8.archunit.fixtures.switchable.defaultmethod.valid.domain;

@FunctionalInterface
public interface OrderSpecification {
	boolean isSatisfiedBy(String order);

	default OrderSpecification and(OrderSpecification other) {
		return order -> isSatisfiedBy(order) && other.isSatisfiedBy(order);
	}
}
