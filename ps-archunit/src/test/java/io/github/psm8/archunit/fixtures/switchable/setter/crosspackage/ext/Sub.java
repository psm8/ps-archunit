package io.github.psm8.archunit.fixtures.switchable.setter.crosspackage.ext;

import io.github.psm8.archunit.fixtures.switchable.setter.crosspackage.model.Base;

public class Sub extends Base {
	private int level;

	public void setLevel(int level) {
		this.level = level;
	}
}
