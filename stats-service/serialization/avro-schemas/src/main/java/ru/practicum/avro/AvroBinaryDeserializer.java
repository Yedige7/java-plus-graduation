package ru.practicum.avro;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;

import java.io.IOException;

public final class AvroBinaryDeserializer {

    private AvroBinaryDeserializer() {
    }

    public static <T extends SpecificRecord> T fromBytes(byte[] data, Class<T> type) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        try {
            T instance = type.getDeclaredConstructor().newInstance();

            SpecificDatumReader<T> reader =
                    new SpecificDatumReader<>(instance.getSchema());

            BinaryDecoder decoder =
                    DecoderFactory.get().binaryDecoder(data, null);

            return reader.read(null, decoder);
        } catch (ReflectiveOperationException | IOException e) {
            throw new RuntimeException("Failed to deserialize Avro record: " + type.getName(), e);
        }
    }
}