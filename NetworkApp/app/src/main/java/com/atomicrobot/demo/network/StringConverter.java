package com.atomicrobot.demo.network;

import com.google.common.io.CharStreams;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class StringConverter implements Converter {
    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            return CharStreams.toString(new InputStreamReader(body.in(), "UTF-8"));
        } catch (Exception ex) {
            throw new ConversionException("Couldn't convert it to a string", ex);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        try {
            return new TypedByteArray("text/plain", ((String) object).getBytes("UTF-8"));
        } catch (Exception ex) {
            throw new IllegalArgumentException();
        }
    }
}
