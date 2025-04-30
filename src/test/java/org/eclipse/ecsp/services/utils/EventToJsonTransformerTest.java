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

import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.services.exceptions.TransformerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test if EventTransformer works fine with sample json")
class EventToJsonTransformerTest {
    private static final ObjectMapper EVENT_JSON_MAPPER =
        ServiceUtil.createJsonMapperForIgniteEvent();
    
    @Test
    void testThatSampleTransformWorksFine() throws IOException {
        String vehicleProfileNotification =
            IOUtils.toString(new ClassPathResource("/modemInfo/vehicleProfileWiFi.json").getInputStream(),
                StandardCharsets.UTF_8);
        IgniteEventImpl igniteEvent =
            EVENT_JSON_MAPPER.readValue(vehicleProfileNotification, IgniteEventImpl.class);
        IgniteEventToJsonTransformer eventTransformer =
            new IgniteEventToJsonTransformer(Paths.get("/modemInfo/spec.json"));
        String json = eventTransformer.transform(igniteEvent);
        assertFalse(StringUtils.isEmpty(json));
        assertEquals(JsonUtils.classpathToObject("/modemInfo/wifi.json"),
            JsonUtils.jsonToObject(json));
    }
    
    @Test
    void testThatTransformFromJsonSpecStringWorksFine() throws IOException {
        String vehicleProfileNotification =
            IOUtils.toString(this.getClass().getResourceAsStream("/modemInfo/vehicleProfileWiFi.json"),
                StandardCharsets.UTF_8);
        IgniteEventImpl igniteEvent =
            EVENT_JSON_MAPPER.readValue(vehicleProfileNotification, IgniteEventImpl.class);
        String transformSpec = "[{\"operation\":\"shift\",\"spec\":{\"Data\":"
            +
            "{\"changeDescriptions\":{\"*\":{\"key\":{\"modemInfo\":{\"@2\":{\"@(6,VehicleId)\":\"VIN\","
            + "\"changed\":{\"iccid\":\"ICCID\",\"eid\":\"EID\"}}}}}},\"customExtension\":"
            + "{\"make\":\"Make\",\"model\":\"Model\"}}}},{\"operation\":\"default\",\"spec\":"
            + "{\"ICCID\":null,\"EID\":null,\"VIN\":null,\"Make\":null,\"Model\":null}}]";
        IgniteEventToJsonTransformer eventTransformer = new IgniteEventToJsonTransformer(transformSpec);
        String json = eventTransformer.transform(igniteEvent);
        assertFalse(StringUtils.isEmpty(json));
        assertEquals(JsonUtils.classpathToObject("/modemInfo/wifi.json"),
            JsonUtils.jsonToObject(json));
    }
    
    @Test
    void testThatExceptionIsThrownInCaseOfMissingResourceFile() {
        Path path = Paths.get("foo");
        Exception e = assertThrows(TransformerException.class, () -> {
            new IgniteEventToJsonTransformer(path);
        });
        assertEquals("Cannot find the specification resource file for json transformer: foo",
            e.getMessage());
    }
}
