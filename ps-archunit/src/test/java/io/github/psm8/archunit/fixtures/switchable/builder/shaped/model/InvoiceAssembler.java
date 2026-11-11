package io.github.psm8.archunit.fixtures.switchable.builder.shaped.model;

public final class InvoiceAssembler {
	private String number;

	public InvoiceAssembler number(String number) {
		this.number = number;
		return this;
	}

	public Invoice build() {
		return new Invoice(number);
	}
}
