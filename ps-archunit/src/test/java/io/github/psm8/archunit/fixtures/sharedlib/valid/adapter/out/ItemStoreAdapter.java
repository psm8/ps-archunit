package io.github.psm8.archunit.fixtures.sharedlib.valid.adapter.out;

import io.github.psm8.archunit.fixtures.sharedlib.valid.application.port.out.ItemStorePort;
import io.github.psm8.archunit.fixtures.sharedlib.valid.domain.model.Item;
import io.github.psm8.archunit.fixtures.sharedlib.valid.lib.RequestTag;

import java.util.List;

final class ItemStoreAdapter implements ItemStorePort {
	@Override
	public List<Item> findAll() {
		return List.of(new Item(RequestTag.of("out")));
	}
}
