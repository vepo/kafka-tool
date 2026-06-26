package io.vepo.kafka.tool.inspect;

public record ConsumerGroupMemberInfo(String consumerId, String clientId, String host, String assignment) {

}
