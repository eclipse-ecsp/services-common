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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.services.entities.CustomData;
import org.eclipse.ecsp.transform.TransformerSerDeException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.util.Map;
import java.util.Optional;

/**
 * Custom data transformer.
 * {@link CustomData}
 * convert the custom data to customExtension {@link Map} format and vice-versa.
 */
public class CustomExtensionTransformer {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(CustomExtensionTransformer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private CustomExtensionTransformer() {}
    
    /**
     * Convert the {@link CustomData} to java Pojo.
     *
     * @param data  eventData containing customExtension.
     * @param clazz deserialize custom data to
     */
    public static void convertCustomExtensionToOutputPojo(AbstractEventData data, Class<?> clazz) {
        Optional<Object> customExtension = data.getCustomExtension();
        try {
            if (customExtension.isPresent()) {
                GenericCustomExtension customExt = (GenericCustomExtension) customExtension.get();
                Map<String, Object> customExtMap = customExt.getCustomData();
                String originJson = OBJECT_MAPPER.writeValueAsString(customExtMap);
                String convertedjson =
                    OBJECT_MAPPER.writeValueAsString(OBJECT_MAPPER.readValue(originJson, clazz));
                data.setCustomExtension(
                    new GenericCustomExtension(JsonMapperUtils.getJsonAsMap(convertedjson)));
            }
        } catch (Exception e) {
            LOGGER.error("Convert custom extension exception {}:  from value :{}  to output pojo {}",
                e, data.getCustomExtension().toString(), clazz);
            throw new TransformerSerDeException("Convert custom extension exception" + e.getMessage());
        }
    }
    
    /**
     * convert custom extension data to Class format and put it back.
     * convert between Map to Object, faster than string to Object
     *
     * @param data  eventData containing customExtension.
     * @param clazz deserialize custom data to
     */
    public static void convertCustomExtensionToOutputPojoFaster(AbstractEventData data, Class<?> clazz) {
        data.getCustomExtension().ifPresent(obj -> {
            GenericCustomExtension customExt = (GenericCustomExtension) obj;
            // get map from CustomExtension
            Map<String, Object> customExtMap = customExt.getCustomData();
            // convert custom data map to new class object
            Object newObj = OBJECT_MAPPER.convertValue(customExtMap, clazz);
            // new class object to map
            Map<String, Object> newMap =
                OBJECT_MAPPER.convertValue(newObj, new TypeReference<Map<String, Object>>() {
                });
            // put map to CustomExtension
            customExt.setCustomData(newMap);
        });
    }
}
