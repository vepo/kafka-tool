//DEPS com.fasterxml.jackson.core:jackson-databind:2.13.2.2
//DEPS com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2
//DEPS org.apache.kafka:kafka-clients:3.2.0

package serializers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
public class JsonSerializer implements Serializer<Object> {

    private ObjectMapper objectMapper;
    public JsonSerializer(){
        objectMapper = JsonMapper.builder()
                                 .findAndAddModules()
                                 .build();
    }

    public byte[] serialize(String topic, Object data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}