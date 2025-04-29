/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and\
 * limitations under the License.
 * 
 * <p>SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.ecsp.services.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.exceptions.TransformerException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for converting java object to json and vice-versa.
 *
 * @author Neerajkumar
 */
@SuppressWarnings("serial")
public class JsonMapperUtils {

    private JsonMapperUtils() {
    }

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(JsonMapperUtils.class);

    private static final Map<String, ObjectMapper> MAPPER_MAP =
        new HashMap<>();

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final SimpleBeanPropertyFilter GENERIC_FILTER =
        SimpleBeanPropertyFilter.serializeAllExcept(EventAttribute.ID,
            EventAttribute.SCHEMA_VERSION, EventAttribute.REQUEST_ID,
            EventAttribute.SOURCE_DEVICE_ID,
            EventAttribute.VEHICLE_ID,
            EventAttribute.MESSAGE_ID, EventAttribute.CORRELATION_ID,
            EventAttribute.BIZTRANSACTION_ID,
            EventAttribute.BENCH_MODE, EventAttribute.RESPONSE_EXPECTED,
            EventAttribute.DEVICE_DELIVERY_CUTOFF, EventAttribute.TIMEZONE,
            EventAttribute.DFF_QUALIFIER,
            EventAttribute.USER_CONTEXT, EventAttribute.LAST_UPDATED_TIME,
            EventAttribute.DUPLICATE_MESSAGE,
            EventAttribute.ECU_TYPE,
            EventAttribute.MQTT_TOPIC);

    private static final SimpleBeanPropertyFilter GENERIC_FILTER_WITH_REQUEST_ID =
        SimpleBeanPropertyFilter.serializeAllExcept(EventAttribute.ID,
            EventAttribute.SCHEMA_VERSION,
            EventAttribute.SOURCE_DEVICE_ID,
            EventAttribute.VEHICLE_ID,
            EventAttribute.MESSAGE_ID, EventAttribute.CORRELATION_ID,
            EventAttribute.BIZTRANSACTION_ID,
            EventAttribute.BENCH_MODE, EventAttribute.RESPONSE_EXPECTED,
            EventAttribute.DEVICE_DELIVERY_CUTOFF, EventAttribute.TIMEZONE,
            EventAttribute.DFF_QUALIFIER,
            EventAttribute.USER_CONTEXT, EventAttribute.LAST_UPDATED_TIME,
            EventAttribute.ECU_TYPE,
            EventAttribute.MQTT_TOPIC);

    private static final SimpleBeanPropertyFilter GENERIC_FILTER_WITH_TIMEZONE =
        SimpleBeanPropertyFilter.serializeAllExcept(EventAttribute.ID,
            EventAttribute.SCHEMA_VERSION, EventAttribute.REQUEST_ID,
            EventAttribute.SOURCE_DEVICE_ID,
            EventAttribute.VEHICLE_ID,
            EventAttribute.MESSAGE_ID, EventAttribute.CORRELATION_ID,
            EventAttribute.BIZTRANSACTION_ID,
            EventAttribute.BENCH_MODE, EventAttribute.RESPONSE_EXPECTED,
            EventAttribute.DEVICE_DELIVERY_CUTOFF,
            EventAttribute.DFF_QUALIFIER,
            EventAttribute.USER_CONTEXT, EventAttribute.LAST_UPDATED_TIME,
            EventAttribute.DUPLICATE_MESSAGE,
            EventAttribute.ECU_TYPE,
            EventAttribute.MQTT_TOPIC);

    private static final SimpleBeanPropertyFilter ORIGIN_FILTER =
        SimpleBeanPropertyFilter.serializeAllExcept(EventAttribute.ORIGIN,
            EventAttribute.USERID, EventAttribute.PARTNER_ID);

    static {

        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSON_MAPPER.setSerializationInclusion(Include.NON_NULL);
        JSON_MAPPER.setFilterProvider(
            new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAll()));
        JSON_MAPPER.registerModule(new JavaTimeModule());
        Map<String, SimpleBeanPropertyFilter> roFilterMap = Map.of(EventAttribute.EVENT_FILTER, GENERIC_FILTER,
                        EventAttribute.RO_RESPONSE_FILTER, ORIGIN_FILTER);

        Map<String, SimpleBeanPropertyFilter> riFilterMap = Map.of(EventAttribute.EVENT_FILTER, GENERIC_FILTER,
                   EventAttribute.REMOTE_INHIBIT_FILTER, ORIGIN_FILTER);
        
        Map<String, SimpleBeanPropertyFilter> roRiFilterMap = Map.of(EventAttribute.EVENT_FILTER, GENERIC_FILTER,
                    EventAttribute.RO_RESPONSE_FILTER, ORIGIN_FILTER,
                    EventAttribute.REMOTE_INHIBIT_FILTER, ORIGIN_FILTER);

        Map<String, SimpleBeanPropertyFilter> defaultFilterMap = Map.of(EventAttribute.EVENT_FILTER, GENERIC_FILTER);
        
        MAPPER_MAP.put(EventAttribute.RO, getMapper(roFilterMap));
        MAPPER_MAP.put(EventAttribute.REMOTE_INHIBIT, getMapper(riFilterMap));
        MAPPER_MAP.put(EventAttribute.RO_RI, getMapper(roRiFilterMap));
        MAPPER_MAP.put(EventAttribute.DEFAULT_FILTER, getMapper(defaultFilterMap));
        MAPPER_MAP.put("GENERIC_FILTER_REQ_ID",
                getMapper(Map.of(EventAttribute.EVENT_FILTER, GENERIC_FILTER_WITH_REQUEST_ID)));
        MAPPER_MAP.put("GENERIC_FILTER_TIMEZONE",
                getMapper(Map.of(EventAttribute.EVENT_FILTER, GENERIC_FILTER_WITH_TIMEZONE)));
    }
    
    private static ObjectMapper getMapper(Map<String, SimpleBeanPropertyFilter> filterMap) {
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        for (Map.Entry<String, SimpleBeanPropertyFilter> entry : filterMap.entrySet()) {
            filterProvider.addFilter(entry.getKey(), entry.getValue());
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        mapper.setFilterProvider(filterProvider);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    public static String applyExcludefilterAndGetAsString(String name, Object object)
        throws JsonProcessingException {
        return MAPPER_MAP.get(name).writeValueAsString(object);
    }
    
    public static byte[] applyExcludefilterAndGetAsByte(String name, Object object)
        throws JsonProcessingException {
        return MAPPER_MAP.get(name).writeValueAsBytes(object);
    }
    
    /**
     * convert provided json string to {@link Map}.
     *
     * @param data json string
     * @return java.util.Map representation of json
     */
    public static Map<String, Object> getJsonAsMap(String data) {
        try {
            return JSON_MAPPER.readValue(data, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            LOGGER.error("Unable to convert String  :{} to map, error :{}", data, e);
            throw new TransformerException("Unable to convert String to Map");
        }
        
    }
    
    /**
     * Convert java object to json string.
     *
     * @param obj java object for json serialization
     * @return serialized json
     */
    public static String getObjectValueAsString(Object obj) {
        try {
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to convert object to json string", e);
            LOGGER.info("Unable to create the class for the object {}", obj.toString());
            return null;
        }
    }

    /**
     * convert object for generic type.
     *
     * @param fromValue object
     * @param toValueTypeRef generic type\
     * @param <T> generic
     *
     * @return converted object typed to generic type.
     *
     * @throws IllegalArgumentException if conversion fails.
     */
    public static <T> T getTypedObjectFromJson(Object fromValue, TypeReference<T> toValueTypeRef)
        throws IllegalArgumentException {
        return JSON_MAPPER.convertValue(fromValue, toValueTypeRef);
    }
}
