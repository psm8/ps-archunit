package io.github.psm8.archunit.fixtures.switchable.setter.behavior.model;

public class Account {
	private String name;

	public Account(String name) {
		this.name = name;
	}

	public void rename(String newName) {
		name = newName;
	}

	public String name() {
		return name;
	}

	public boolean settled(String reference) {
		return name.equals(reference);
	}
}
