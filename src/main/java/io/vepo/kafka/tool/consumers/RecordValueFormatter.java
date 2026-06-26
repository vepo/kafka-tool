package io.vepo.kafka.tool.consumers;

import com.google.protobuf.Message;

public final class RecordValueFormatter {

    public static String formatValue(Object valueObject) {
        if (valueObject instanceof byte[] bytes) {
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }
        if (valueObject instanceof org.apache.avro.generic.GenericData.Record avro) {
            return avro.toString();
        }
        if (valueObject instanceof Message proto) {
            return ProtobufHelper.toJson(proto);
        }
        if (valueObject instanceof java.util.LinkedHashMap<?, ?> map) {
            return map.toString();
        }
        return String.valueOf(valueObject);
    }

    private RecordValueFormatter() {}

}
