package io.vepo.kafka.tool.inspect;

public record ConnectionResult(boolean success, String message, KafkaAdminService.BrokerStatus status) {

    public static ConnectionResult connected(String brokerName) {
        return new ConnectionResult(true,
                                    "Connected to \"" + brokerName + "\". Opening main window…",
                                    KafkaAdminService.BrokerStatus.CONNECTED);
    }

    public static ConnectionResult failed(String message) {
        return new ConnectionResult(false, message, KafkaAdminService.BrokerStatus.IDLE);
    }

    public static ConnectionResult failed(Exception error) {
        return failed(friendlyMessage(error));
    }

    public static ConnectionResult testOk(String brokerName) {
        return new ConnectionResult(true,
                                    "Successfully reached \"" + brokerName + "\" at the configured bootstrap servers.",
                                    KafkaAdminService.BrokerStatus.IDLE);
    }

    private static String friendlyMessage(Exception error) {
        if (error == null) {
            return "Connection failed. Check bootstrap servers and that Kafka is running.";
        }
        var message = error.getMessage();
        if (message != null && !message.isBlank()) {
            return message + "\n\nCheck bootstrap servers, network access, and that Kafka is running.";
        }
        return "Connection failed. Check bootstrap servers and that Kafka is running.";
    }

}
