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

import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.services.ServiceCommonTestConfig;
import org.eclipse.ecsp.services.ServicesTestBase;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.eclipse.ecsp.domain.Constants.USER_ID_UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * service util test.
 *
 * @author Neerajkumar
 */
@SpringBootTest
@ContextConfiguration(classes = ServiceCommonTestConfig.class)
@TestPropertySource("classpath:/application.properties")
class ServiceUtilTest extends ServicesTestBase {
    @Autowired
    private ServiceUtil serviceUtil;
    
    @MockitoBean
    private VehicleProfileClient vehicleProfileClient;
    
    @BeforeEach
    void setup() {
        // This method is called before each test case
    }
    
    @Test
    void testGetHeaderMap() {
        Map<String, String> headerMap =
            serviceUtil.getHeaderMap("sessionIdTest", "platformResponseIdTest");
        assertEquals("sessionIdTest", headerMap.get("SessionId"));
        assertEquals("platformResponseIdTest", headerMap.get("PlatformResponseId"));
        assertEquals("dummy-api-key", headerMap.get("x-api-key"));
        assertEquals("application/json", headerMap.get("content-type"));
    }
    
    @Test
    void testGetHeaderMap2() {
        Map<String, String> headerMap =
            serviceUtil.getHeaderMap("sessionIdTest", "platformResponseIdTest", "partner123");
        assertEquals("sessionIdTest", headerMap.get("SessionId"));
        assertEquals("platformResponseIdTest", headerMap.get("PlatformResponseId"));
        assertEquals("partner123", headerMap.get(EventAttribute.PARTNER_ID));
        assertEquals("dummy-api-key", headerMap.get("x-api-key"));
        assertEquals("application/json", headerMap.get("content-type"));
    }
    
    @Test
    void testGetHeaderMapForTypes() {
        Map<String, String> headerMap =
            serviceUtil.getHeaderMapForTypes("sessionIdTest", "platformResponseIdTest", "type2");
        assertEquals("sessionIdTest", headerMap.get("SessionId"));
        assertEquals("platformResponseIdTest", headerMap.get("PlatformResponseId"));
        assertEquals("dummy-api-key", headerMap.get("x-api-key"));
        assertEquals("application/json", headerMap.get("content-type"));
    }
    
    @Test
    void testGetHeaderMapForTypes2() {
        Map<String, String> headerMap =
            serviceUtil.getHeaderMapForTypes("sessionIdTest", "platformResponseIdTest", "type2",
                "partner123");
        assertEquals("sessionIdTest", headerMap.get("SessionId"));
        assertEquals("platformResponseIdTest", headerMap.get("PlatformResponseId"));
        assertEquals("partner123", headerMap.get(EventAttribute.PARTNER_ID));
        assertEquals("dummy-api-key", headerMap.get("x-api-key"));
        assertEquals("application/json", headerMap.get("content-type"));
    }
    
    @Test
    void testGetUserContextInfoGetUserByVin() {
        String vehicleId = "VIN001";
        String userId = "UID666666";
        
        when(vehicleProfileClient.getVehicleProfileAttribute(ArgumentMatchers.eq(vehicleId),
            ArgumentMatchers.any(VehicleProfileAttribute.class), ArgumentMatchers.eq(true)))
            .thenReturn(Optional.of(userId));
        
        List<UserContext> userContextList = serviceUtil.getUserContextInfo(vehicleId);
        
        assertEquals(1, userContextList.size());
        
        UserContext uc = userContextList.get(0);
        assertEquals(userId, uc.getUserId());
        assertEquals("VO", uc.getRole());
    }
    
    @Test
    void testGetUserContextInfoUnableGetUserByVin() {
        String vehicleId = "VIN002";
        
        when(vehicleProfileClient.getVehicleProfileAttribute(ArgumentMatchers.eq(vehicleId),
            ArgumentMatchers.any(VehicleProfileAttribute.class), ArgumentMatchers.eq(true)))
            .thenReturn(Optional.ofNullable(null));
        
        List<UserContext> userContextList = serviceUtil.getUserContextInfo(vehicleId);
        
        assertEquals(1, userContextList.size());
        UserContext uc = userContextList.get(0);
        assertEquals(USER_ID_UNKNOWN, uc.getUserId());
    }
    
    @Test
    void testGetUserContextInfoGetEmptyByVin() {
        String vehicleId = "VIN002";
        
        when(vehicleProfileClient.getVehicleProfileAttribute(ArgumentMatchers.eq(vehicleId),
            ArgumentMatchers.any(VehicleProfileAttribute.class), ArgumentMatchers.eq(true)))
            .thenReturn(Optional.ofNullable(""));
        
        List<UserContext> userContextList = serviceUtil.getUserContextInfo(vehicleId);
        
        assertEquals(1, userContextList.size());
        UserContext uc = userContextList.get(0);
        assertEquals(USER_ID_UNKNOWN, uc.getUserId());
    }
    
    @Test
    void getHeaderMap() {
        Map<String, String> map = serviceUtil.getHeaderMap("session-id", "platform-response-id");
        assertEquals("session-id", map.get(EventAttribute.SESSION_ID));
        assertEquals("platform-response-id", map.get(EventAttribute.PLATFORM_RESPONSE_ID));
    }
    
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {
    
    }
}
