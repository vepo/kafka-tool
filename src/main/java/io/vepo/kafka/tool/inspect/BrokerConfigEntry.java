package io.vepo.kafka.tool.inspect;

public record BrokerConfigEntry(String name, String value, String source, boolean sensitive) {

    public String displayValue() {
        return sensitive ? "********" : value;
    }

}
