package io.vepo.kafka.tool.consumers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.settings.KeySerializer;
import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.ValueSerializer;

class KeyFormatterTest {

    @Test
    void formatIntegerKey() {
        assertEquals("1", KeyFormatter.format(new byte[] { 0, 0, 0, 1 }, KeySerializer.INTEGER));
    }

    @Test
    void formatNullKey() {
        assertEquals("null", KeyFormatter.format(null, KeySerializer.STRING));
    }

    @Test
    void formatStringKey() {
        assertEquals("hello", KeyFormatter.format("hello".getBytes(), KeySerializer.STRING));
    }

}
