package io.github.psm8.archunit.fixtures.invalid.cycle.application.service.alpha;

import io.github.psm8.archunit.fixtures.invalid.cycle.application.service.beta.BetaService;

public final class AlphaService {
    private final BetaService betaService;

    public AlphaService(BetaService betaService) {
        this.betaService = betaService;
    }

    public BetaService betaService() {
        return betaService;
    }
}
