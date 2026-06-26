package io.vepo.kafka.tool.settings;

import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validate;
import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validateBootstrapServers;
import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validateName;
import static io.vepo.kafka.tool.settings.KafkaBrokerValidator.validateSchemaRegistryUrl;
import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static io.vepo.kafka.tool.support.gherkin.Fixture.fixture;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KafkaBrokerValidatorTest {

    private static final String FEATURE = "Kafka broker validation";

    @Test
    void acceptsValidBroker() throws Throwable {
        try (var env = feature(FEATURE).scenario("Accept a valid broker profile").start()) {
            var broker = env.given("a broker with name, bootstrap servers, and schema registry URL",
                    new KafkaBroker("Local", "localhost:9092", "http://localhost:8081"));
            var result = env.when("the broker is validated", () -> validate(broker, of()));
            env.then("validation succeeds", () -> assertTrue(result.valid()));
        }
    }

    @Test
    void rejectsBlankName() throws Throwable {
        try (var env = feature(FEATURE).scenario("Reject a blank broker name").start()) {
            var broker = env.given("a broker with an empty name", new KafkaBroker("", "localhost:9092", ""));
            var result = env.when("the name is validated", () -> validateName("", broker, of()));
            env.then("validation fails", () -> assertFalse(result.valid()));
        }
    }

    @Test
    void rejectsDuplicateName() throws Throwable {
        try (var env = feature(FEATURE).scenario("Reject a duplicate broker name").start()) {
            var existing = env.given("an existing broker named Local", new KafkaBroker("Local", "localhost:9092", ""));
            var duplicate = env.given(fixture("another broker named local", new KafkaBroker("local", "localhost:9093", "")));
            var result = env.when("the duplicate name is validated", () -> validateName("local", duplicate, of(existing)));
            env.then("validation fails", () -> assertFalse(result.valid()));
        }
    }

    @Test
    void rejectsInvalidBootstrapServers() throws Throwable {
        try (var env = feature(FEATURE).scenario("Reject invalid bootstrap server values").start()) {
            env.given("bootstrap server strings with missing, malformed, or out-of-range values");
            env.when("each value is validated");
            env.then("validation fails for empty, host-only, trailing-comma, and invalid port values", () -> {
                assertFalse(validateBootstrapServers("").valid());
                assertFalse(validateBootstrapServers("localhost").valid());
                assertFalse(validateBootstrapServers("localhost:9092,").valid());
                assertFalse(validateBootstrapServers("localhost:70000").valid());
            });
        }
    }

    @Test
    void acceptsMultipleBootstrapServers() throws Throwable {
        try (var env = feature(FEATURE).scenario("Accept comma-separated bootstrap servers").start()) {
            env.given("multiple host:port pairs");
            var result = env.when("the bootstrap servers are validated",
                    () -> validateBootstrapServers("localhost:9092,127.0.0.1:9093"));
            env.then("validation succeeds", () -> assertTrue(result.valid()));
        }
    }

    @Test
    void acceptsBlankSchemaRegistryUrl() throws Throwable {
        try (var env = feature(FEATURE).scenario("Accept a missing schema registry URL").start()) {
            env.given("no schema registry URL");
            env.when("validation runs");
            env.then("blank and null URLs are accepted", () -> {
                assertTrue(validateSchemaRegistryUrl("").valid());
                assertTrue(validateSchemaRegistryUrl(null).valid());
            });
        }
    }

    @Test
    void rejectsInvalidSchemaRegistryUrl() throws Throwable {
        try (var env = feature(FEATURE).scenario("Reject an invalid schema registry URL").start()) {
            env.given("URLs without http/https or with an invalid scheme");
            env.when("each URL is validated");
            env.then("validation fails", () -> {
                assertFalse(validateSchemaRegistryUrl("localhost:8081").valid());
                assertFalse(validateSchemaRegistryUrl("ftp://localhost:8081").valid());
            });
        }
    }
}
