package io.github.psm8.archunit.fixtures.switchable.builder.generated.model;

public final class Order {
	private final String id;
	private final int quantity;

	private Order(String id, int quantity) {
		this.id = id;
		this.quantity = quantity;
	}

	public static OrderBuilder builder() {
		return new OrderBuilder();
	}

	public String id() {
		return id;
	}

	public int quantity() {
		return quantity;
	}

	public static final class OrderBuilder {
		private String id;
		private int quantity;

		OrderBuilder() {
		}

		public OrderBuilder id(String id) {
			this.id = id;
			return this;
		}

		public OrderBuilder quantity(int quantity) {
			this.quantity = quantity;
			return this;
		}

		public Order build() {
			return new Order(id, quantity);
		}
	}
}
