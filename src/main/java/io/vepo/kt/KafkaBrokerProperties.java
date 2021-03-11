package io.vepo.kt;

import java.io.Serializable;
import java.util.Objects;

public class KafkaBrokerProperties implements Serializable {
    private static final long serialVersionUID = 2097907317083752687L;

    private String bootStrapServers;
    private String schemaRegistryUrl;
    private String topic;

    public String getBootStrapServers() {
        return bootStrapServers;
    }

    public void setBootStrapServers(String bootStrapServers) {
        this.bootStrapServers = bootStrapServers;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bootStrapServers, schemaRegistryUrl, topic);
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
        KafkaBrokerProperties other = (KafkaBrokerProperties) obj;
        return Objects.equals(bootStrapServers, other.bootStrapServers)
                && Objects.equals(schemaRegistryUrl, other.schemaRegistryUrl)
                && Objects.equals(topic, other.topic);
    }

    @Override
    public String toString() {
        return String.format("KafkaBrokerProperties [bootStrapServers=%s, schemaRegistryUrl=%s, topic=%s]",
                             bootStrapServers, schemaRegistryUrl, topic);
    }

}
