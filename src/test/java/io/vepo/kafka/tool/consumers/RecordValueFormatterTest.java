package io.vepo.kafka.tool.consumers;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

class RecordValueFormatterTest {

    @Test
    void formatsByteArrayAsUtf8() throws Throwable {
        try (var env = feature("Record value formatting").scenario("Format byte array as UTF-8").start()) {
            env.then("bytes become string", () -> assertEquals("hello", RecordValueFormatter.formatValue("hello".getBytes())));
        }
    }

    @Test
    void formatsLinkedHashMapAsString() throws Throwable {
        try (var env = feature("Record value formatting").scenario("Format JSON map").start()) {
            var map = new LinkedHashMap<String, Object>();
            map.put("a", 1);
            env.then("map toString is used", () -> assertTrue(RecordValueFormatter.formatValue(map).contains("a=1")));
        }
    }

    @Test
    void formatsNullAsString() throws Throwable {
        try (var env = feature("Record value formatting").scenario("Format null value").start()) {
            env.then("null becomes literal", () -> assertEquals("null", RecordValueFormatter.formatValue(null)));
        }
    }

}
