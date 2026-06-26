package io.vepo.kafka.tool.settings.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.vepo.kafka.tool.settings.Entry;
import io.vepo.kafka.tool.settings.KeySerializer;
import io.vepo.kafka.tool.settings.KafkaSettings;
import io.vepo.kafka.tool.settings.SerializerSettings;
import io.vepo.kafka.tool.settings.Settings;
import io.vepo.kafka.tool.settings.UiSettings;
import io.vepo.kafka.tool.settings.ValueSerializer;

public class SettingsService {

    public KafkaSettings kafka() {
        return Settings.kafka();
    }

    public SerializerSettings serializers() {
        return Settings.serializers();
    }

    public UiSettings ui() {
        return Settings.ui();
    }

    public CompletableFuture<KafkaSettings> updateKafka(Consumer<KafkaSettings> fn) {
        return Settings.updateKafka(fn);
    }

    public void updateKeySerializer(Entry<String, KeySerializer> entry) {
        Settings.updateKeySerializer(entry);
    }

    public void updateUi(Consumer<UiSettings> fn) {
        Settings.updateUi(fn);
    }

    public void updateValueSerializer(Entry<String, ValueSerializer> entry) {
        Settings.updateValueSerializer(entry);
    }

}
