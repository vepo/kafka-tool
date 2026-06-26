package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConnectionResultTest {

    @Test
    void connectedResultIsSuccessful() throws Throwable {
        try (var env = feature("Broker connection").scenario("Report successful connect").start()) {
            var result = env.when("connection succeeds", ConnectionResult::connected);
            env.then("result is successful", () -> {
                assertTrue(result.success());
                assertTrue(result.message().contains("Connected"));
            });
        }
    }

    @Test
    void failedResultIsNotSuccessful() throws Throwable {
        try (var env = feature("Broker connection").scenario("Report failed connect").start()) {
            var result = env.when("connection fails",
                                  () -> ConnectionResult.failed("Broker unreachable"));
            env.then("result is a failure", () -> {
                assertFalse(result.success());
                assertTrue(result.message().contains("unreachable"));
            });
        }
    }

}
