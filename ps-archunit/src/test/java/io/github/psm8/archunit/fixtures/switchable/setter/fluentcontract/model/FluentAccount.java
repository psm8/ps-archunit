package io.github.psm8.archunit.fixtures.switchable.setter.fluentcontract.model;

public class FluentAccount implements Fluent {
	private String name;

	@Override
	public FluentAccount name(String name) {
		this.name = name;
		return this;
	}
}
