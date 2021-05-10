package io.vepo.kafka.tool.controls.helpers;

import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

import io.vepo.kafka.tool.controls.helpers.pojo.Pojos;
import io.vepo.kafka.tool.controls.helpers.pojo.Pojos.Simple;
import io.vepo.kafka.tool.controls.helpers.pojo.Pojos.Complex.Corpus;

class ProtobufHelperTest {

    @Test
    void test() {
	var message = ProtobufHelper.toJson(Pojos.Complex.newBuilder()
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
                                                         .setBolleanValue(true)
                                                         .setData(ByteString.copyFrom(new byte[] {'a', 'b', 'c'}))
                                                         .setDoubleValue(2.6)
                                                         .addCorpusA(Corpus.LOCAL)
                                                         .addCorpusA(Corpus.IMAGES)
                                                         .setFloatValue(5.12f)
                                                         .putMap("A", Simple.newBuilder()
                                                        	            .setValue((int)'a')
                                                        	            .build())
                                                         .build());
	System.out.println(message);
    }

}
