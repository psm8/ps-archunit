package io.github.psm8.archunit.fixtures.switchable.setter.crosspackage.model;

public class Base {
	private int level;

	void setLevel(int level) {
		this.level = level;
	}

	int level() {
		return level;
	}
}
