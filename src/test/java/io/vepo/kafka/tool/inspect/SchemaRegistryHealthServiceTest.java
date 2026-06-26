package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SchemaRegistryHealthServiceTest {

    @Test
    void blankUrlIsNotConfigured() throws Throwable {
        try (var env = feature("Schema Registry health").scenario("Blank URL is not configured").start()) {
            env.then("status is not configured",
                     () -> assertEquals("Not configured", SchemaRegistryHealthService.statusForUrl("  ")));
        }
    }

    @Test
    void normalizesTrailingSlash() throws Throwable {
        try (var env = feature("Schema Registry health").scenario("Normalize trailing slash").start()) {
            env.then("base URL has no trailing slash",
                     () -> assertEquals("http://localhost:8081", SchemaRegistryHealthService.normalizedBase("http://localhost:8081/")));
        }
    }

}
