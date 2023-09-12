package dev.vepo.kafka.tool.inspect;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class KafkaMessage {
    private final byte[] key;
    private final String value;

    public KafkaMessage(byte[] key, String value) {
        this.key = key;
        this.value = value;
    }

    public byte[] getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaMessage that = (KafkaMessage) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return String.format("KafkaMessage[key=%s, value=%s]", Arrays.toString(key), value);
    }
}
