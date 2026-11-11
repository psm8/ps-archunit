package io.github.psm8.archunit.fixtures.switchable.setter.inbase.model;

public class NamedAccount implements Named {
	private String name;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	String name() {
		return name;
	}
}
