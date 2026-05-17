package ru.practicum.avro;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class AvroBinarySerializer {

    private AvroBinarySerializer() {
    }

    public static byte[] toBytes(SpecificRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("record is null");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var writer = new SpecificDatumWriter<SpecificRecord>(record.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

            writer.write(record, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize Avro record: " + record.getClass().getName(), e);
        }
    }
}