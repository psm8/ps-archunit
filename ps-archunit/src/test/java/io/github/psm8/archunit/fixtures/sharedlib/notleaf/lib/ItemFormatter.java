package io.github.psm8.archunit.fixtures.sharedlib.notleaf.lib;

import io.github.psm8.archunit.fixtures.sharedlib.notleaf.adapter.in.ItemView;
import io.github.psm8.archunit.fixtures.sharedlib.notleaf.domain.model.Item;

public final class ItemFormatter {
	private ItemFormatter() {
	}

	public static String format(Item item, ItemView view) {
		return item.id() + view.render();
	}
}
