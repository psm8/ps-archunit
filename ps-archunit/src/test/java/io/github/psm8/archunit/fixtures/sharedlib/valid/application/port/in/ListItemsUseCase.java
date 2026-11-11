package io.github.psm8.archunit.fixtures.sharedlib.valid.application.port.in;

import io.github.psm8.archunit.fixtures.sharedlib.valid.domain.model.Item;

import java.util.List;

public interface ListItemsUseCase {
	List<Item> list();
}
