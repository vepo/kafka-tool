package io.vepo.kafka.tool.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KafkaSettings implements Settings<KafkaSettings>, Cloneable {
    private List<KafkaBroker> brokers;

    public KafkaSettings() {
        this.brokers = new ArrayList<>();
    }

    public List<KafkaBroker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<KafkaBroker> brokers) {
        this.brokers = brokers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KafkaSettings that = (KafkaSettings) o;
        return Objects.equals(brokers, that.brokers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokers);
    }

    @Override
    public String toString() {
        return String.format("KafkaSettings [brokers=%s]", brokers);
    }

    @Override
    public KafkaSettings clone() {
        try {
            return (KafkaSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
