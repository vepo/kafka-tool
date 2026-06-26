package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConnectionResultTest {

    @Test
    void connectedResultIsSuccessful() throws Throwable {
        try (var env = feature("Broker connection").scenario("Report successful connect").start()) {
            var result = env.when("connection succeeds", () -> ConnectionResult.connected("local"));
            env.then("result is successful", () -> {
                assertTrue(result.success());
                assertTrue(result.message().contains("Connected"));
            });
        }
    }

    @Test
    void failedExceptionWithBlankMessageUsesGenericText() throws Throwable {
        try (var env = feature("Broker connection").scenario("Failed exception with blank message").start()) {
            var result = env.when("connection fails with blank message",
                                  () -> ConnectionResult.failed(new Exception("  ")));
            env.then("generic message is returned", () -> assertTrue(result.message().contains("Connection failed")));
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

    @Test
    void testOkLeavesBrokerIdle() throws Throwable {
        try (var env = feature("Broker connection").scenario("Test connection leaves broker idle").start()) {
            var result = env.when("test connection succeeds", () -> ConnectionResult.testOk("local"));
            env.then("result is successful but status is idle", () -> {
                assertTrue(result.success());
                assertEquals(KafkaAdminService.BrokerStatus.IDLE, result.status());
            });
        }
    }

}
