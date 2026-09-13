package io.github.psm8.archunit.fixtures.invalid.onion.application.service;

import io.github.psm8.archunit.fixtures.invalid.onion.adapter.out.IllegalAdapter;

public final class IllegalApplicationService {
    private final IllegalAdapter adapter;

    public IllegalApplicationService(IllegalAdapter adapter) {
        this.adapter = adapter;
    }

    public IllegalAdapter adapter() {
        return adapter;
    }
}
