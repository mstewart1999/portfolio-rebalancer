package com.pbalancer.client.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JSONHelper
{
    private static final JavaTimeModule jsr310 = new JavaTimeModule();

    public static String toJson(final Object o) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(jsr310);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        return mapper.writeValueAsString(o);
    }

    public static <T> T fromJson(final String val, final Class<T> c) throws JsonMappingException, JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(jsr310);
        return mapper.readValue(val, c);
    }

}
