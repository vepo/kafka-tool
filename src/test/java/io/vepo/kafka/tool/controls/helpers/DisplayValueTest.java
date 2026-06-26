package io.vepo.kafka.tool.controls.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class DisplayValueTest {

    @Test
    void blankStringShowsDash() {
        assertEquals("-", DisplayValue.ofString(""));
        assertEquals("-", DisplayValue.ofString("   "));
    }

    @Test
    void emptyOptionalShowsDash() {
        assertEquals("-", DisplayValue.of(Optional.empty()));
    }

    @Test
    void nullableObjectShowsDash() {
        assertEquals("-", DisplayValue.ofNullable(null));
        assertEquals("42", DisplayValue.ofNullable(42));
        assertEquals("STABLE", DisplayValue.ofNullable(Optional.of("STABLE")));
    }

    @Test
    void unwrapsPresentOptional() {
        assertEquals("STABLE", DisplayValue.of(Optional.of("STABLE")));
    }

}
