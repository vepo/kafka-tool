package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BrokerLogDirSummaryTest {

    @Test
    void formatsBytesAsHumanReadable() throws Throwable {
        try (var env = feature("Log directory summary").scenario("Format byte sizes").start()) {
            env.then("kilobytes are formatted", () -> assertEquals("1.0 KB", BrokerLogDirSummary.formatBytes(1024)));
            env.then("gigabytes are formatted",
                     () -> assertEquals("1.00 GB", BrokerLogDirSummary.formatBytes(1024L * 1024 * 1024)));
            env.then("negative bytes show dash", () -> assertEquals("—", BrokerLogDirSummary.formatBytes(-1)));
            env.then("bytes are formatted", () -> assertEquals("512 B", BrokerLogDirSummary.formatBytes(512)));
        }
    }

}
