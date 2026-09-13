package io.github.psm8.archunit.fixtures.invalid.cycle.application.service.beta;

import io.github.psm8.archunit.fixtures.invalid.cycle.application.service.alpha.AlphaService;

public final class BetaService {
    private final AlphaService alphaService;

    public BetaService(AlphaService alphaService) {
        this.alphaService = alphaService;
    }

    public AlphaService alphaService() {
        return alphaService;
    }
}
