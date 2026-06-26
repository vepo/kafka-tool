package io.vepo.kafka.tool.inspect;

public record ClusterBrokerInfo(int brokerId, String host, int port, String rack, String role) {

    public String endpoint() {
        return "%s:%d".formatted(host, port);
    }

    public String rackDisplay() {
        return rack == null || rack.isBlank() ? "—" : rack;
    }

}
