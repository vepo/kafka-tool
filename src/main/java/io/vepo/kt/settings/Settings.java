package io.vepo.kt.settings;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.kt.KafkaTool;

public interface Settings<T extends Settings<?>> {
    static final Logger logger = LoggerFactory.getLogger(KafkaTool.class);
    static final ObjectMapper mapper = new ObjectMapper().enable(INDENT_OUTPUT);
    static final String KAFKA_TOOL_FOLDER = ".kafka-tool";

    public static UiSettings ui() {
        return loadProperties(UiSettings.class, UiSettings.UI_SETTINGS_FILE)
                                                                            .orElseGet(() -> new UiSettings(new WindowSettings(512,
                                                                                                                               512)));
    }

    public static KafkaSettings kafka() {
        return loadProperties(KafkaSettings.class, KafkaSettings.KAFKA_SETTINGS_FILE)
                                                                                     .orElseGet(() -> new KafkaSettings(new ArrayList<>()));
    }

    public void save();

    static <T extends Settings<?>> void save(String filename, T settings) {

        Path propertiesFolder = Paths.get(KAFKA_TOOL_FOLDER);
        if (!propertiesFolder.toFile().exists()) {
            propertiesFolder.toFile().mkdir();
        }

        Path propertiesPath = propertiesFolder.resolve(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(propertiesPath, CREATE, TRUNCATE_EXISTING)) {
            writer.write(mapper.writeValueAsString(settings));
        } catch (IOException e) {
            logger.error("Error saving file!", e);
        }
    }

    static <T> Optional<T> loadProperties(Class<T> clz, String filename) {
        Path propertiesFolder = Paths.get(KAFKA_TOOL_FOLDER);
        if (!propertiesFolder.toFile().exists()) {
            propertiesFolder.toFile().mkdir();
        }

        Path propertiesPath = propertiesFolder.resolve(filename);
        if (propertiesPath.toFile().exists()) {
            try (BufferedReader reader = Files.newBufferedReader(propertiesPath)) {
                return Optional.of(mapper.readValue(reader.lines().collect(joining()), clz));
            } catch (IOException e) {
                logger.error("Error reading file!", e);
            }
        }
        return Optional.empty();
    }

}
