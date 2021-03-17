package io.vepo.kt;

public record KafkaMessage(String key, String value) {

    public String getKey(){
        return key;
    }

    public String getValue(){
        return value;
    }
}
