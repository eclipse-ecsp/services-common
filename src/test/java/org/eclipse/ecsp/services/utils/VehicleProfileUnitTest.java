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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.AssociatedVehicles;
import org.eclipse.ecsp.services.ServiceCommonTestConfig;
import org.eclipse.ecsp.services.ServicesTestBase;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.exceptions.AssociationFailedException;
import org.eclipse.ecsp.services.exceptions.DisassociationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = ServiceCommonTestConfig.class)
@TestPropertySource("classpath:/application.properties")
@DisplayName("Unit tests for VehicleProfileClient")
class VehicleProfileUnitTest extends ServicesTestBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final long EXPECTED_DATE_1 = 1572436732000L;
    public static final long EXPECTED_DATE_2 = 1572436732000L;

    @Autowired
    private VehicleProfileClient vehicleProfileClient;
    @MockitoBean
    private RestTemplate restTemplate;
    
    @BeforeEach
    void setup() throws JsonProcessingException {
        Object vpJson = JsonUtils.classpathToObject("/vehicleprofile/vehicleProfile.json");
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>(JsonUtils.toJsonString(vpJson), HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        String str =
            "{\"message\": \"SUCCESS\",\"data\": [{\"vehicleId\": \"33005da244055381\", \"role\": \"VEHICLE_OWNER\""
                + ",\"createdOn\": 1572436732000,\"status\": \"CONNECTED\",\"statusChangeTimestamp\": 1572436732000}]}";
        
        AssociatedVehicles data = OBJECT_MAPPER.readValue(str, AssociatedVehicles.class);
        ResponseEntity<AssociatedVehicles> response =
            new ResponseEntity<AssociatedVehicles>(data, HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(any(URI.class), eq(AssociatedVehicles.class)))
            .thenReturn(response);
        
        String disassocaitedRes = "{\"message\": \"SUCCESS\",\"data\": true}";
        ResponseEntity<String> disassocaitedResponse =
            new ResponseEntity<String>(disassocaitedRes, HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
            eq(String.class))).thenReturn(disassocaitedResponse);
        
    }
    
    @Test
    void testSanityFetchNickName() {
        Optional<String> val =
            vehicleProfileClient.getVehicleProfileAttrWithClientId("foo", VehicleProfileAttribute.NAME);
        assertEquals("150_2001", val.get());
    }
    
    @Test
    void testSanityGetVehicleProfileJsonWithClientId() throws JsonProcessingException {
        Optional<String> vehicleProfileOptional =
            vehicleProfileClient.getVehicleProfileJsonWithClientId("foo");
        JsonNode vehicleProfile = OBJECT_MAPPER.readTree(vehicleProfileOptional.get());
        String userId = vehicleProfile.get("authorizedUsers").get(0).get("userId").asText();
        assertEquals("string", userId);
    }
    
    @Test
    void testGetAssociatedVehiclesForUser() {
        AssociatedVehicles associatedVehicles =
            vehicleProfileClient.getAssociatedVehiclesForUser("test");
        assertNotNull(associatedVehicles);
        assertEquals("SUCCESS", associatedVehicles.getMessage());
        assertEquals("VEHICLE_OWNER", associatedVehicles.getData().get(0).getRole());
        assertEquals("33005da244055381", associatedVehicles.getData().get(0).getVehicleId());
        assertEquals(EXPECTED_DATE_1, associatedVehicles.getData().get(0).getCreatedOn().longValue());
        assertEquals(EXPECTED_DATE_2,
            associatedVehicles.getData().get(0).getStatusChangeTimestamp().longValue());
    }
    
    @Test
    void testDisAssociatedVehiclesFromUser() throws DisassociationFailedException {
        assertTrue(vehicleProfileClient.disassociateVehicle("testUser", "testVehicle"));
    }
    
    @Test
    void testAssociatedVehiclesFromUser() throws AssociationFailedException {
        assertTrue(
            vehicleProfileClient.associateVehicle("testUser", "testVehicle", "COMPLETED_STAGE_3"));
    }
}
