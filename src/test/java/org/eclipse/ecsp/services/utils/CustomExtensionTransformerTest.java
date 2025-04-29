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

import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.transform.TransformerSerDeException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomExtensionTransformerTest {
    @Test
    void convertCustomExtensionToOutputPojo() {
        MockEventData eventData = new MockEventData();
        eventData.setName("event-data-name");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", "iid");
        map.put("response", "SUCCESS");
        map.put("version", Version.V4_0);
        map.put("other", "other_value");
        eventData.setCustomExtension(new GenericCustomExtension(map));
        
        CustomExtensionTransformer.convertCustomExtensionToOutputPojo(eventData, MockPojo.class);
        
        Map<String, Object> customData =
            ((GenericCustomExtension) eventData.getCustomExtension().get()).getCustomData();
        assertFalse(customData.containsKey("other"));
        assertEquals(customData.get("version"), Version.V4_0.getValue());
    }
    
    @Test
    void testConvertCustomExtensionToOutputPojoFast() {
        MockEventData eventData = new MockEventData();
        eventData.setName("event-data-name");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", "iid");
        map.put("response", "SUCCESS");
        map.put("version", Version.V9_9);
        map.put("other", "other_value");
        eventData.setCustomExtension(new GenericCustomExtension(map));
        
        CustomExtensionTransformer.convertCustomExtensionToOutputPojoFaster(eventData, MockPojo.class);
        
        Map<String, Object> customData =
            ((GenericCustomExtension) eventData.getCustomExtension().get()).getCustomData();
        assertFalse(customData.containsKey("other"));
        assertEquals(customData.get("version"), Version.V9_9.getValue());
    }
    
    @Test
    void convertCustomExtensionToOutputPojo_customExtensionNotPresent() {
        MockEventData eventData = new MockEventData();
        eventData.setName("event-data-name");
        eventData.setCustomExtension(new GenericCustomExtension(new LinkedHashMap<>()));
        
        assertDoesNotThrow(() -> {
            CustomExtensionTransformer.convertCustomExtensionToOutputPojo(eventData, MockPojo.class);
        });
    }
    
    @Test
    @Disabled("unable prepare data")
    void convertCustomExtensionToOutputPojo_Exception() {
        MockEventData eventData = new MockEventData();
        eventData.setName("event-data-name");
        
        assertThrows(TransformerSerDeException.class, () -> {
            CustomExtensionTransformer.convertCustomExtensionToOutputPojo(eventData, MockPojo.class);
        });
    }
    
    static class MockEventData extends AbstractEventData {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    static class MockPojo {
        private String id;
        private String response;
        private Version version;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getResponse() {
            return response;
        }
        
        public void setResponse(String response) {
            this.response = response;
        }
        
        public Version getVersion() {
            return version;
        }
        
        public void setVersion(Version version) {
            this.version = version;
        }
    }
}