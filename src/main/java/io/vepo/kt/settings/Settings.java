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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.kt.KafkaTool;

public interface Settings<T extends Settings<?>> {
    static final String KAFKA_SETTINGS_FILE = "kafka-properties.json";
    static final String KAFKA_TOOL_FOLDER = ".kafka-tool";
    static final Path KAFKA_TOOL_CONFIG_PATH = Paths.get(System.getProperty("user.home"), KAFKA_TOOL_FOLDER);
    static final Logger logger = LoggerFactory.getLogger(KafkaTool.class);
    static final ObjectMapper mapper = new ObjectMapper().enable(INDENT_OUTPUT);
    static final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    static final String UI_SETTINGS_FILE = "ui-properties.json";

    public static KafkaSettings kafka() {
        return loadProperties(KafkaSettings.class, KafkaSettings.KAFKA_SETTINGS_FILE)
                                                                                     .orElseGet(KafkaSettings::new);
    }

    static <T> Optional<T> loadProperties(Class<T> clz, String filename) {
        if (!KAFKA_TOOL_CONFIG_PATH.toFile().exists()) {
            KAFKA_TOOL_CONFIG_PATH.toFile().mkdir();
        }

        Path propertiesPath = KAFKA_TOOL_CONFIG_PATH.resolve(filename);
        if (propertiesPath.toFile().exists()) {
            try (BufferedReader reader = Files.newBufferedReader(propertiesPath)) {
                return Optional.of(mapper.readValue(reader.lines().collect(joining()), clz));
            } catch (IOException e) {
                logger.error("Error reading file!", e);
            }
        }
        return Optional.empty();
    }

    static <T extends Settings<?>> void save(String filename, T settings) {
        if (!KAFKA_TOOL_CONFIG_PATH.toFile().exists()) {
            KAFKA_TOOL_CONFIG_PATH.toFile().mkdir();
        }

        Path propertiesPath = KAFKA_TOOL_CONFIG_PATH.resolve(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(propertiesPath, CREATE, TRUNCATE_EXISTING)) {
            writer.write(mapper.writeValueAsString(settings));
        } catch (IOException e) {
            logger.error("Error saving file!", e);
        }
    }

    public static UiSettings ui() {
        return loadProperties(UiSettings.class, UiSettings.UI_SETTINGS_FILE)
                                                                            .orElseGet(UiSettings::new);
    }

    public static void updateKafka(Consumer<KafkaSettings> fn) {
        saveExecutor.execute(() -> {
            KafkaSettings settings = kafka();
            fn.accept(settings);
            save(KAFKA_SETTINGS_FILE, settings);
        });
    }

    public static void updateUi(Consumer<UiSettings> fn) {
        saveExecutor.execute(() -> {
            UiSettings settings = ui();
            fn.accept(settings);
            save(UI_SETTINGS_FILE, settings);
        });
    }

}
