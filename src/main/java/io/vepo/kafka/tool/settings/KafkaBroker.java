package io.vepo.kafka.tool.settings;

import java.util.Objects;

public class KafkaBroker implements Cloneable {
    private String name;
    private String bootStrapServers;
    private String schemaRegistryUrl;

    public KafkaBroker() {}

    public KafkaBroker(String name, String bootStrapServers, String schemaRegistryUrl) {
        this.name = name;
        this.bootStrapServers = bootStrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @Override
    public KafkaBroker clone() {
        try {
            return (KafkaBroker) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloud not clone!", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KafkaBroker that = (KafkaBroker) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(bootStrapServers, that.bootStrapServers)
                && Objects.equals(schemaRegistryUrl, that.schemaRegistryUrl);
    }

    public String getBootStrapServers() {
        return bootStrapServers;
    }

    public String getName() {
        return name;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bootStrapServers, schemaRegistryUrl);
    }

    public boolean hasSchemaRegistry() {
        return Objects.nonNull(this.schemaRegistryUrl) && !this.schemaRegistryUrl.isBlank();
    }

    public void setBootStrapServers(String bootStrapServers) {
        this.bootStrapServers = bootStrapServers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @Override
    public String toString() {
        return String.format("KafkaBroker [name=%s, bootStrapServers=%s, schemaRegistryUrl=%s]", name, bootStrapServers,
                             schemaRegistryUrl);
    }
}