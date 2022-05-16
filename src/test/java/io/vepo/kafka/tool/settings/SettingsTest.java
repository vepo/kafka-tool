package io.vepo.kafka.tool.settings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Settings")
class SettingsTest {

    @BeforeEach
    void setup() throws IOException {
        Files.walk(Settings.KAFKA_TOOL_CONFIG_PATH)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
    }

    @Test
    void loadTest() {
        Assertions.assertTrue(Settings.kafka().getBrokers().isEmpty());
        Settings.updateKafka(kafkaSettings -> {
            kafkaSettings.getBrokers().add(new KafkaBroker("Local", "localhost:9092", "http://localhost:8080"))
        });
        Assertions.assertFalse(Settings.kafka().getBrokers().isEmpty());
    }
}