package io.vepo.kafka.tool.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SerializerSettings implements Settings<SerializerSettings>, Cloneable {
	    private Map<String, Serializer> usedSerializer;

    public SerializerSettings() {
	usedSerializer = new HashMap<>();
    }

    public Map<String, Serializer> getUsedSerializer() {
	return usedSerializer;
    }

    public void setUsedSerializer(Map<String, Serializer> usedSerializer) {
	this.usedSerializer = usedSerializer;
    }

    @Override
    public int hashCode() {
	return Objects.hash(usedSerializer);
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
	return Objects.equals(usedSerializer, other.usedSerializer);
    }

    @Override
    public String toString() {
	return String.format("SerializerSetting [usedSerializer=%s]", usedSerializer);
    }

}
