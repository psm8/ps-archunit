package io.github.psm8.archunit.fixtures.switchable.setter.override.model;

import org.springframework.beans.factory.BeanNameAware;

public class NamedBean implements BeanNameAware {
	private String beanName;

	@Override
	public void setBeanName(String name) {
		beanName = name;
	}

	String beanName() {
		return beanName;
	}
}
