package io.vepo.kafka.tool.settings.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vepo.kafka.tool.settings.KafkaBroker;
import io.vepo.kafka.tool.settings.Settings;

class SettingsServiceTest {

    @BeforeEach
    void setup() throws IOException {
        if (Settings.KAFKA_TOOL_CONFIG_PATH.toFile().exists()) {
            Files.walk(Settings.KAFKA_TOOL_CONFIG_PATH)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

    @Test
    void updateKafkaDelegatesToSettings() throws InterruptedException {
        var service = new SettingsService();
        assertTrue(service.kafka().getBrokers().isEmpty());
        service.updateKafka(kafkaSettings -> kafkaSettings.getBrokers()
                                                          .add(new KafkaBroker("Local", "localhost:9092", "http://localhost:8080")))
               .thenAccept(kafka -> assertFalse(kafka.getBrokers().isEmpty()));
        Thread.sleep(500);
    }

}
