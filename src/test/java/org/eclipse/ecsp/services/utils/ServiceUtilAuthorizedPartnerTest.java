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


import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.domain.AuthorizedPartnerDetail;
import org.eclipse.ecsp.domain.ServiceClaim;
import org.eclipse.ecsp.services.ServiceCommonTestConfig;
import org.eclipse.ecsp.services.ServicesTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import static org.eclipse.ecsp.domain.Constants.DTF_VEHICLE_PROFILE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * service util test.
 *
 * @author Neerajkumar
 */
@SpringBootTest
@ContextConfiguration(classes = ServiceCommonTestConfig.class)
@TestPropertySource("classpath:/application.properties")
class ServiceUtilAuthorizedPartnerTest extends ServicesTestBase {
    
    public static final int NANO_OF_SECOND_33 = 333;
    public static final int FIFTY_NINE = 59;
    public static final int HOUR_23 = 23;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int NANO_OF_SECOND_666 = 666;
    public static final int YEAR_2020 = 2020;
    public static final int DAY_OF_MONTH_19 = 19;
    public static final int HOUR_17 = 17;
    @Autowired
    private ServiceUtil serviceUtil;
    
    @Autowired
    private VehicleProfileClient vehicleProfileClient;
    
    @Autowired
    private RestTemplate restTemplate;
    MockRestServiceServer mockRestServiceServer;

    @BeforeEach
    void setup() {
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }
    
    @Test
    void getAuthorizedPartnerDetail() throws IOException {
        String vehicleId = "VIN003";
        String serviceId = "ROTRUNK";
        String jsonContent = IOUtils.toString(
            this.getClass().getResourceAsStream("/serviceUtils/authorizedPartners.json"), StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now();
        
        jsonContent = jsonContent
            .replace("{start001}",
                ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0, 0, NANO_OF_SECOND_33,
                    ZoneId.of("GMT")).format(DTF_VEHICLE_PROFILE))
            .replace("{expire001}",
                ZonedDateTime.of(now.getYear(),
                    now.getMonthValue(), now.getDayOfMonth(), HOUR_23, FIFTY_NINE, FIFTY_NINE,
                    NANO_OF_SECOND_666, ZoneId.of("GMT")).format(DTF_VEHICLE_PROFILE));

        mockRestServiceServer.expect(requestTo(containsString("/v1.0/vehicleProfiles/" + vehicleId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonContent, MediaType.APPLICATION_JSON));

        AuthorizedPartnerDetail detail =
            serviceUtil.getAuthorizedPartnerDetail(vehicleId, "IGNITE_TEST_EVENT", serviceId);
        
        assertTrue(detail.isChannelOutboundRequired());
        assertEquals(TWO, detail.getOutboundDetails().size());
        assertEquals("LATAM", detail.getSoldRegion());
    }
    
    @Test
    void getAuthorizedPartnerDetail_Empty() throws IOException {
        String vehicleId = "VIN003";
        String serviceId = "ROTRUNK";
        String jsonContent = IOUtils.toString(
            this.getClass().getResourceAsStream("/serviceUtils/authorizedPartners_empty.json"),
            StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now();
        
        jsonContent = jsonContent
            .replace("{start001}",
                ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0, 0, NANO_OF_SECOND_33,
                    ZoneId.of("GMT")).format(DTF_VEHICLE_PROFILE))
            .replace("{expire001}",
                ZonedDateTime.of(now.getYear(), now.getMonthValue(),
                    now.getDayOfMonth(), HOUR_23, FIFTY_NINE, FIFTY_NINE,
                    NANO_OF_SECOND_666, ZoneId.of("GMT")).format(DTF_VEHICLE_PROFILE));

        mockRestServiceServer.expect(requestTo(containsString("/v1.0/vehicleProfiles/" + vehicleId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonContent, MediaType.APPLICATION_JSON));

        AuthorizedPartnerDetail detail =
            serviceUtil.getAuthorizedPartnerDetail(vehicleId, "IGNITE_TEST_EVENT", serviceId);
        
        assertTrue(detail.isChannelOutboundRequired());
        assertEquals(0, detail.getOutboundDetails().size());
        assertNull(detail.getSoldRegion());
    }
    
    @Test
    void isServiceClaimsValid() {
        Map<String, ServiceClaim> serviceClaimMap = new HashMap<>();
        serviceClaimMap.put("ROTRUNK",
            new ServiceClaim("2020-03-01T16:00:00.666+0000", "2020-03-31T17:59:59.666+0000"));
        
        boolean isValid =
            ReflectionTestUtils.invokeMethod(serviceUtil, "isServiceClaimsValid", serviceClaimMap,
                "ROTRUNK", LocalDateTime.of(YEAR_2020, THREE, DAY_OF_MONTH_19, HOUR_17, 0, 0));
        
        assertTrue(isValid);
    }
    
    @Test
    void isServiceClaimsValid_start_fail() {
        Map<String, ServiceClaim> serviceClaimMap = new HashMap<>();
        serviceClaimMap.put("ROTRUNK",
            new ServiceClaim("2020-03-19T17:30:00.666+0000", "2020-03-19T18:59:59.666+0000"));
        
        boolean isValid =
            ReflectionTestUtils.invokeMethod(serviceUtil, "isServiceClaimsValid", serviceClaimMap,
                "ROTRUNK", LocalDateTime.of(YEAR_2020, THREE, DAY_OF_MONTH_19, HOUR_17, 0, 0));
        
        assertFalse(isValid);
    }
    
    @Test
    void isServiceClaimsValid_expire_fail() {
        Map<String, ServiceClaim> serviceClaimMap = new HashMap<>();
        serviceClaimMap.put("ROTRUNK",
            new ServiceClaim("2020-03-19T16:00:00.666+0000", "2020-03-19T16:59:59.666+0000"));
        
        boolean isValid =
            ReflectionTestUtils.invokeMethod(serviceUtil, "isServiceClaimsValid", serviceClaimMap,
                "ROTRUNK", LocalDateTime.of(YEAR_2020, THREE, DAY_OF_MONTH_19, HOUR_17, 0, 0));
        
        assertFalse(isValid);
    }
    
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {
    
    }
}
