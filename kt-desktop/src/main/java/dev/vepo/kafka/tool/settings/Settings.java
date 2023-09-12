package dev.vepo.kafka.tool.settings;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface Settings<T extends Settings<?>> {
    static final String KAFKA_SETTINGS_FILE = "kafka-properties.json";
    static final String KAFKA_TOOL_FOLDER = ".kafka-tool";
    static final Path KAFKA_TOOL_CONFIG_PATH = Paths.get(System.getProperty("user.home"), KAFKA_TOOL_FOLDER);
    static final Logger logger = LoggerFactory.getLogger(Settings.class);
    static final ObjectMapper mapper = new ObjectMapper().enable(INDENT_OUTPUT);
    static final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    static final String UI_SETTINGS_FILE = "ui-properties.json";
    static final String SERIALIZERS_SETTINGS_FILE = "serializers.json";

    static KafkaSettings kafka() {
        return loadProperties(KafkaSettings.class, KafkaSettings.KAFKA_SETTINGS_FILE).orElseGet(KafkaSettings::new);
    }

    @FunctionalInterface
    interface IoSupplier<V> {
        V get() throws IOException;
    }

    static <V> Optional<V> handleIoException(IoSupplier<V> fn) {
        try {
            return Optional.ofNullable(fn.get());
        } catch (IOException e) {
            logger.error("Error reading file!", e);
            return Optional.empty();
        }
    }

    static <T> Optional<T> loadProperties(Class<T> clz, String filename) {
        if (!KAFKA_TOOL_CONFIG_PATH.toFile().exists()) {
            KAFKA_TOOL_CONFIG_PATH.toFile().mkdir();
        }

        var propertiesPath = KAFKA_TOOL_CONFIG_PATH.resolve(filename);
        if (propertiesPath.toFile().exists()) {
            try (var reader = Files.newBufferedReader(propertiesPath)) {
                return Optional.of(reader.lines()
                                         .collect(joining()))
                               .filter(Predicate.not(String::isBlank))
                               .flatMap(value -> handleIoException(() -> mapper.readValue(value, clz)));
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

        var propertiesPath = KAFKA_TOOL_CONFIG_PATH.resolve(filename);
        try (var writer = Files.newBufferedWriter(propertiesPath, CREATE, TRUNCATE_EXISTING)) {
            writer.write(mapper.writeValueAsString(settings));
        } catch (IOException e) {
            logger.error("Error saving file!", e);
        }
    }

    public static UiSettings ui() {
        return loadProperties(UiSettings.class, UI_SETTINGS_FILE).orElseGet(UiSettings::new);
    }

    public static SerializerSettings serializers() {
        return loadProperties(SerializerSettings.class, SERIALIZERS_SETTINGS_FILE).orElseGet(SerializerSettings::new);
    }

    public static CompletableFuture<KafkaSettings> updateKafka(Consumer<KafkaSettings> fn) {
        saveExecutor.execute(() -> {
            var settings = kafka();
            fn.accept(settings);
            save(KAFKA_SETTINGS_FILE, settings);
        });
        return CompletableFuture.supplyAsync(Settings::kafka, saveExecutor);
    }

    public static void updateUi(Consumer<UiSettings> fn) {
        saveExecutor.execute(() -> {
            var settings = ui();
            fn.accept(settings);
            save(UI_SETTINGS_FILE, settings);
        });
    }

    public static void updateValueSerializer(Entry<String, ValueSerializer> entry) {
        saveExecutor.execute(() -> {
            var settings = serializers();
            settings.getUsedValueSerializer().put(entry.key(), entry.value());
            save(SERIALIZERS_SETTINGS_FILE, settings);
        });
    }

    public static void updateKeySerializer(Entry<String, KeySerializer> entry) {
        saveExecutor.execute(() -> {
            var settings = serializers();
            settings.getUsedKeySerializer().put(entry.key(), entry.value());
            save(SERIALIZERS_SETTINGS_FILE, settings);
        });
    }

}
