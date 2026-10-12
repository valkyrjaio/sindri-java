package io.sindri.tests.fixtures.event.provider;

/**
 * Collides by simple name with the imported io.sindri.tests.fixtures.event.TestEventFixture, so a
 * reader that lets a same-package class outrank an import resolves the wrong class.
 */
public final class TestEventFixture {}
