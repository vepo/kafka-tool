package io.vepo.kafka.tool.inspect;

public record ConnectionResult(boolean success, String message, KafkaAdminService.BrokerStatus status) {

    public static ConnectionResult connected() {
        return new ConnectionResult(true, "Connected successfully.", KafkaAdminService.BrokerStatus.CONNECTED);
    }

    public static ConnectionResult failed(String message) {
        return new ConnectionResult(false, message, KafkaAdminService.BrokerStatus.IDLE);
    }

    public static ConnectionResult testOk() {
        return new ConnectionResult(true, "Connection successful.", KafkaAdminService.BrokerStatus.IDLE);
    }

}
