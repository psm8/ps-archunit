package io.github.psm8.archunit.fixtures.switchable.setter.named.model;

public class Account {
	private String name;
	private String secret;

	public void setName(String name) {
		this.name = name;
	}

	private void setSecret(String secret) {
		this.secret = secret;
	}

	static void setGlobalDefault(String ignored) {
	}

	String describe() {
		setSecret(name);
		return secret;
	}
}
