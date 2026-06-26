package io.vepo.kafka.tool.consumers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.settings.KeySerializer;

class KeyFormatterTest {

    @Test
    void formatIntegerKey() {
        assertEquals("1", KeyFormatter.format(new byte[] { 0, 0, 0, 1 }, KeySerializer.INTEGER));
    }

    @Test
    void formatInvalidIntegerLength() {
        assertEquals("Invalid integer", KeyFormatter.format(new byte[] { 1, 2 }, KeySerializer.INTEGER));
    }

    @Test
    void formatNullKey() {
        assertEquals("null", KeyFormatter.format(null, KeySerializer.STRING));
    }

    @Test
    void formatNullSerializer() {
        assertEquals("No serializer selected", KeyFormatter.format("x".getBytes(), null));
    }

    @Test
    void formatStringKey() {
        assertEquals("hello", KeyFormatter.format("hello".getBytes(), KeySerializer.STRING));
    }

}
