package io.vepo.kafka.tool.support.gherkin;

public record Fixture<T>(String description, T value) {
    public static <T> Fixture<T> fixture(String description, T value) {
        return new Fixture<>(description, value);
    }
}
