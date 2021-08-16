package net.wade.autotests.utilities;

import cloudflow.streamlets.avro.AvroCodec;
import org.apache.avro.specific.SpecificRecordBase;
import scala.util.Try;

public class CloudFlowSerializer {

    public static <T extends SpecificRecordBase> byte[] toByteArray(T record) {
        AvroCodec<T> codec = new AvroCodec<>(record.getSchema());
        return codec.encode(record);
    }

    public static <T extends SpecificRecordBase> Try<T> fromByteArray(byte[] record, T schema) {
        AvroCodec<T> codec = new AvroCodec<>(schema.getSchema());
        return codec.decode(record);
    }
}
