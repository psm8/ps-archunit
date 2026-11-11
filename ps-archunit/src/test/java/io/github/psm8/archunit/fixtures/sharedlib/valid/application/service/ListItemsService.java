package io.github.psm8.archunit.fixtures.sharedlib.valid.application.service;

import io.github.psm8.archunit.fixtures.sharedlib.valid.application.port.in.ListItemsUseCase;
import io.github.psm8.archunit.fixtures.sharedlib.valid.application.port.out.ItemStorePort;
import io.github.psm8.archunit.fixtures.sharedlib.valid.domain.model.Item;

import java.util.List;

public final class ListItemsService implements ListItemsUseCase {
	private final ItemStorePort store;

	public ListItemsService(ItemStorePort store) {
		this.store = store;
	}

	@Override
	public List<Item> list() {
		return store.findAll();
	}
}
