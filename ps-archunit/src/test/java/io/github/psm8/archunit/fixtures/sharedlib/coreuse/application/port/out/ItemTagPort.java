package io.github.psm8.archunit.fixtures.sharedlib.coreuse.application.port.out;

import io.github.psm8.archunit.fixtures.sharedlib.coreuse.lib.RequestTag;

public interface ItemTagPort {
	String tag(RequestTag tag);
}
