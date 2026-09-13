# Use ArchUnit's built-in onion architecture as the minimal baseline

We use ArchUnit's built-in `OnionArchitecture` as the `minimal` profile because
the official library model matches the installed hexagonal guidance at the
coarse domain/application/adapter boundary. Ports remain inside the
application layer, and custom checks are limited to hexagonal conventions that
the built-in rule cannot distinguish.

The trade-off is deliberate: built-in onion rules are easier to understand and
maintain, but they do not distinguish driving from driven ports or validate
port naming. Those checks remain opt-in in `standard`.
