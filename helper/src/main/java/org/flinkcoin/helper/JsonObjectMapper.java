package org.flinkcoin.helper;

/*-
 * #%L
 * Flink - Helper
 * %%
 * Copyright (C) 2021 - 2022 Flink Foundation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.flinkcoin.helper.JsonSerializers.LocalDateDeserializer;
import org.flinkcoin.helper.JsonSerializers.LocalDateSerializer;
import org.flinkcoin.helper.JsonSerializers.LocalTimeDeserializer;
import org.flinkcoin.helper.JsonSerializers.LocalTimeSerializer;
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
