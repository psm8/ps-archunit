package io.github.psm8.archunit.cli;

import java.nio.file.Path;

record ParsedConfiguration(Tier tier, String basePackage, Object layout, Path source) {
}
