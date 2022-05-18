//DEPS org.apache.kafka:kafka-clients:3.2.0

package serializers;

import org.apache.kafka.common.serialization.Serializer;
import com.google.protobuf.Message;

public class ProtobufSerializer implements Serializer<Message> {

    public byte[] serialize(String topic, Message data) {
        return data.toByteArray();
    }
}