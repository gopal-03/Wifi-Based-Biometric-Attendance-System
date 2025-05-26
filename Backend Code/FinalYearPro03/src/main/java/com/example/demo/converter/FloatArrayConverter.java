package com.example.demo.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.ByteBuffer;

@Converter(autoApply = true)
public class FloatArrayConverter implements AttributeConverter<float[], byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(attribute.length * 4);
        for (float f : attribute) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    @Override
    public float[] convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) return null;
        ByteBuffer buffer = ByteBuffer.wrap(dbData);
        float[] array = new float[dbData.length / 4];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }
        return array;
    }
}
