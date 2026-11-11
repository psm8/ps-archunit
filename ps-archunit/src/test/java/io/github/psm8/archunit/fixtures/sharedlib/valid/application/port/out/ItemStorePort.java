package io.github.psm8.archunit.fixtures.sharedlib.valid.application.port.out;

import io.github.psm8.archunit.fixtures.sharedlib.valid.domain.model.Item;

import java.util.List;

public interface ItemStorePort {
	List<Item> findAll();
}
