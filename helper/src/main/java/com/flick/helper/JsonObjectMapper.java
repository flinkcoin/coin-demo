package com.flick.helper;

import com.flick.helper.JsonSerializers.LocalDateDeserializer;
import com.flick.helper.JsonSerializers.LocalDateSerializer;
import com.flick.helper.JsonSerializers.LocalTimeDeserializer;
import com.flick.helper.JsonSerializers.LocalTimeSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonObjectMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonObjectMapper.class);

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();

        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer());
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer());

        OBJECT_MAPPER.registerModule(timeModule);

        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ObjectMapper getMapper() {
        return OBJECT_MAPPER;
    }
}
