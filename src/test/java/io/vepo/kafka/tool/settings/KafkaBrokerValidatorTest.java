package io.vepo.kafka.tool.settings;

import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validate;
import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validateBootstrapServers;
import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validateName;
import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validateSchemaRegistryUrl;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KafkaBrokerValidatorTest {

    @Test
    void acceptsValidBroker() {
        var broker = new KafkaBroker("Local", "localhost:9092", "http://localhost:8081");

        assertTrue(validate(broker, of()).valid());
    }

    @Test
    void rejectsBlankName() {
        var broker = new KafkaBroker("", "localhost:9092", "");

        assertFalse(validateName("", broker, of()).valid());
    }

    @Test
    void rejectsDuplicateName() {
        var existing = new KafkaBroker("Local", "localhost:9092", "");
        var duplicate = new KafkaBroker("local", "localhost:9093", "");

        assertFalse(validateName("local", duplicate, of(existing)).valid());
    }

    @Test
    void rejectsInvalidBootstrapServers() {
        assertFalse(validateBootstrapServers("").valid());
        assertFalse(validateBootstrapServers("localhost").valid());
        assertFalse(validateBootstrapServers("localhost:9092,").valid());
        assertFalse(validateBootstrapServers("localhost:70000").valid());
    }

    @Test
    void acceptsMultipleBootstrapServers() {
        assertTrue(validateBootstrapServers("localhost:9092,127.0.0.1:9093").valid());
    }

    @Test
    void acceptsBlankSchemaRegistryUrl() {
        assertTrue(validateSchemaRegistryUrl("").valid());
        assertTrue(validateSchemaRegistryUrl(null).valid());
    }

    @Test
    void rejectsInvalidSchemaRegistryUrl() {
        assertFalse(validateSchemaRegistryUrl("localhost:8081").valid());
        assertFalse(validateSchemaRegistryUrl("ftp://localhost:8081").valid());
    }
}
