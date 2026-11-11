package io.github.psm8.archunit.fixtures.switchable.setter.fluent.model;

public class Account {
	private String name;

	public Account name(String name) {
		this.name = name;
		return this;
	}

	public String name() {
		return name;
	}
}
