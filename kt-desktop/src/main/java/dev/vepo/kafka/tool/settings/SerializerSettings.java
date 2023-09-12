package dev.vepo.kafka.tool.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SerializerSettings implements Settings<SerializerSettings>, Cloneable {
    private Map<String, ValueSerializer> usedValueSerializer;
    private Map<String, KeySerializer> usedKeySerializer;

    public SerializerSettings() {
        usedValueSerializer = new HashMap<>();
        usedKeySerializer = new HashMap<>();
    }

    public Map<String, ValueSerializer> getUsedValueSerializer() {
        return usedValueSerializer;
    }

    public void setUsedValueSerializer(Map<String, ValueSerializer> usedValueSerializer) {
        this.usedValueSerializer = usedValueSerializer;
    }

    public Map<String, KeySerializer> getUsedKeySerializer() {
        return usedKeySerializer;
    }

    public void setUsedKeySerializer(Map<String, KeySerializer> usedKeySerializer) {
        this.usedKeySerializer = usedKeySerializer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(usedKeySerializer, usedValueSerializer);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SerializerSettings other = (SerializerSettings) obj;
        return Objects.equals(usedKeySerializer, other.usedKeySerializer) &&
                Objects.equals(usedValueSerializer, other.usedValueSerializer);
    }

    @Override
    public String toString() {
        return String.format("SerializerSetting [usedKeySerializer=%s, usedSerializer=%s]", usedKeySerializer, usedValueSerializer);
    }

}
