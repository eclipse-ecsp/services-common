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
import org.eclipse.ecsp.services.ServiceCommonTestConfig;
import org.eclipse.ecsp.services.ServicesTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;


@SpringBootTest
@ContextConfiguration(classes = ServiceCommonTestConfig.class)
@TestPropertySource("classpath:/application.properties")
@DisplayName("Unit tests for SettingsManagerClient")
class SettingsManagerClientUnitTest extends ServicesTestBase {
    
    @Autowired
    private SettingsManagerClient settingsManagerClient;
    
    @MockitoBean
    private RestTemplate restTemplate;
    
    @BeforeEach
    void setup() {
        // This method is called before each test case
    }
    
    @Test
    void testGetConfigurationObject() {
        Object vpJson = JsonUtils.classpathToObject("/settingsManager/settingsManagerGet.json");
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>(JsonUtils.toJsonString(vpJson), HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq(String.class)))
            .thenReturn(responseEntity);

        Map<String, Object> configurationObject =
            settingsManagerClient.getSettingsManagerConfigurationObject("tushar123", "vehicleId",
                "UpdateEcallPhoneNumbers", "settings[0].Data.configurationObject");
        String expected = "{phoneList=null, phoneListMessages=["
                + "{\"enableCallType\":true,\"primaryPhoneNumber\":\"12345678901\","
                + "\"secondaryPhoneNumber\":\"12345678902\",\"callTypeEnum\":\"SOS\"},"
                + "{\"enableCallType\":false,\"primaryPhoneNumber\":\"12345678903\","
                + "\"secondaryPhoneNumber\":\"12345678904\",\"callTypeEnum\":\"Assist1\"}]}"
        ;
        assertEquals(expected, configurationObject.toString());
    }
    
    @Test
    void testGetConfigurationObject_Exception() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq(String.class)))
            .thenReturn(responseEntity);
        
        Map<String, Object> configurationObject =
            settingsManagerClient.getSettingsManagerConfigurationObject("tushar123", "vehicleId",
                "UpdateEcallPhoneNumbers", "settings[0].Data.configurationObject");
        
        assertNull(configurationObject);
    }
    
}
