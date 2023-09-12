package dev.vepo.kafka.tool.controls.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;

import dev.vepo.kafka.tool.controls.helpers.pojo.Pojos;
import dev.vepo.kafka.tool.controls.helpers.pojo.Pojos.Simple;
import dev.vepo.kafka.tool.controls.helpers.pojo.Pojos.Complex.Corpus;

class ProtobufHelperTest {

    private static ObjectMapper mapper = new ObjectMapper();
    @Test
    void test() {
        var actual = ProtobufHelper.toJsonNode(Pojos.Complex.newBuilder()
                .setId(5L)
                .setSimple(Simple.newBuilder()
                        .setValue(32)
                        .build())
                .addSimples(Simple.newBuilder()
                        .setValue(8)
                        .build())
                .addSimples(Simple.newBuilder()
                        .setValue(9)
                        .build())
                .setValue(21)
                .setCorpus(Corpus.NEWS)
                .addTags("tag-1")
                .addTags("tag-2")
                .setUsername("username")
                .setBooleanValue(true)
                .setData(ByteString.copyFrom(new byte[]{'a', 'b', 'c'}))
                .setDoubleValue(2.6)
                .addCorpusA(Corpus.LOCAL)
                .addCorpusA(Corpus.IMAGES)
                .setFloatValue(5.12f)
                .putMap("A", Simple.newBuilder().setValue(17).build())
                .putMap("B", Simple.newBuilder().setValue(12).build())
                .setEmail("user@proto.com")
                .build());

        var expected = mapper.createObjectNode();
        expected.put("id", 5L);
        expected.put("username", "username");
        expected.put("email", "user@proto.com");
        expected.put("value", 21);
        expected.set("simple", mapper.createObjectNode().put("value", 32));
        expected.put("corpus", "NEWS");
        expected.set("tags", mapper.createArrayNode().add("tag-1").add("tag-2"));
        expected.putArray("simples")
                .add(mapper.createObjectNode().put("value", 8))
                .add(mapper.createObjectNode().put("value", 9));
        expected.set("corpusA", mapper.createArrayNode().add("LOCAL").add("IMAGES"));
        expected.put("booleanValue", true);
        expected.put("data", "abc".getBytes());
        expected.put("doubleValue", 2.6);
        expected.put("floatValue", 5.12f);
        var map = mapper.createObjectNode();
        map.set("A", mapper.createObjectNode().put("value", 17));
        map.set("B", mapper.createObjectNode().put("value", 12));
        expected.set("map", map);
        Assertions.assertEquals(expected, actual);
    }

}
