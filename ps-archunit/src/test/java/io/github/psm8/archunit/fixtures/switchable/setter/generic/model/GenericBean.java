package io.github.psm8.archunit.fixtures.switchable.setter.generic.model;

import org.springframework.fake.NamedSupport;

public class GenericBean implements NamedSupport<String> {
	private String name;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	String name() {
		return name;
	}
}
