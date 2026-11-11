package io.github.psm8.archunit.fixtures.switchable.builder.voidbuild.model;

public final class Workflow {
	private String step;

	public Workflow step(String step) {
		this.step = step;
		return this;
	}

	public void build() {
		System.out.println(step);
	}
}
