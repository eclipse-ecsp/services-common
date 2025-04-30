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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.VehicleProfile;
import org.eclipse.ecsp.services.ServiceCommonTestConfig;
import org.eclipse.ecsp.services.ServicesTestBase;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.entities.VehicleProfileOnDemandAttribute;
import org.eclipse.ecsp.services.exceptions.AssociationFailedException;
import org.eclipse.ecsp.services.exceptions.DisassociationFailedException;
import org.eclipse.ecsp.services.exceptions.VehicleProfileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = ServiceCommonTestConfig.class)
@TestPropertySource("classpath:/application.properties")
@DisplayName("Unit tests for VehicleProfileClient")
class VehicleProfileClientUnitTest extends ServicesTestBase {
    public static final int EXPECTED_30 = 13;
    public static final int INT10 = 10;
    public static final String CLIENT_ID = "client-id";
    @Autowired
    private VehicleProfileClient vehicleProfileClient;
    
    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        // This method is called before each test case
    }

    private void mockVehicleProfile() {
        Object vpJson = JsonUtils.classpathToObject("/vehicleprofile/vehicleProfileGet.json");
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>(JsonUtils.toJsonString(vpJson), HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), org.mockito.ArgumentMatchers.eq(String.class)))
            .thenReturn(responseEntity);
    }
    
    @Test
    void testSanityFetchMakeModel() {
        mockVehicleProfile();
        
        Map<VehicleProfileAttribute, Optional<String>> vals =
            vehicleProfileClient.getVehicleProfileAttributes("foo",
                VehicleProfileAttribute.MAKE,
                VehicleProfileAttribute.MODEL,
                VehicleProfileAttribute.DESTINATION_COUNTRY,
                VehicleProfileAttribute.LICENSE_PLATE,
                VehicleProfileAttribute.SOLD_REGION);
        Optional<String> optMake = vals.get(VehicleProfileAttribute.MAKE);
        assertTrue(optMake.isPresent());
        assertEquals("brandName", optMake.get());
        
        Optional<String> optModel = vals.get(VehicleProfileAttribute.MODEL);
        assertTrue(optModel.isPresent());
        assertEquals("modelName", optModel.get());
        
        Optional<String> optDc = vals.get(VehicleProfileAttribute.DESTINATION_COUNTRY);
        assertTrue(optDc.isPresent());
        assertEquals("US", optDc.get());
        
        Optional<String> optLp = vals.get(VehicleProfileAttribute.LICENSE_PLATE);
        assertTrue(optLp.isPresent());
        assertEquals("A11 0001", optLp.get());
        
        Optional<String> optSr = vals.get(VehicleProfileAttribute.SOLD_REGION);
        assertTrue(optSr.isPresent());
        assertNotNull(optSr.get());
    }
    
    @Test
    void testSanityFetchUserId() {
        mockVehicleProfile();
        
        Optional<String> val =
            vehicleProfileClient.getVehicleProfileAttribute("foo", VehicleProfileAttribute.USERID);
        
        assertTrue(val.isPresent());
        assertEquals("string", val.get());
    }
    
    @Test
    void testSanityFetchMsisdn() {
        mockVehicleProfile();
        
        Optional<String> val =
            vehicleProfileClient.getVehicleProfileAttribute("foo", VehicleProfileAttribute.MSISDN);
        
        assertTrue(val.isPresent());
        assertEquals("12345", val.get());
    }
    
    @Test
    void getVehicleProfileAttribute_ExceptionIgnore() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), org.mockito.ArgumentMatchers.eq(String.class)))
            .thenReturn(responseEntity);
        
        Optional<String> val =
            vehicleProfileClient.getVehicleProfileAttribute("foo", VehicleProfileAttribute.USERID,
                true);
        
        assertTrue(val.isPresent());
        assertEquals("", val.get());
    }
    
    @Test
    void getVehicleProfileAttribute_Exception() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), org.mockito.ArgumentMatchers.eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttribute("foo", VehicleProfileAttribute.USERID, false);
        });
    }
    
    @Test
    void getVehicleProfileAttrWithClientId_ExceptionIgnore() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(contains("clientId"),
            eq(String.class)
        )).thenReturn(responseEntity);
        
        Optional<String> val = vehicleProfileClient.getVehicleProfileAttrWithClientId(CLIENT_ID,
            VehicleProfileAttribute.USERID, true);
        
        assertTrue(val.isPresent());
        assertEquals("", val.get());
    }
    
    @Test
    void getVehicleProfileAttrWithClientId_ResposneException() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttrWithClientId(CLIENT_ID,
                VehicleProfileAttribute.USERID, false);
        });
    }
    
    @Test
    void getVehicleProfileAttrWithClientId_JsonException() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttrWithClientId(CLIENT_ID,
                VehicleProfileAttribute.USERID, false);
        });
    }
    
    @Test
    void getVehicleProfile_Success() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\":{\"vin\":\"test-vin\"}}", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Optional<VehicleProfile> val = vehicleProfileClient.getVehicleProfile(CLIENT_ID);
        
        assertFalse(val.isEmpty());
        assertEquals("test-vin", val.get().getVin());
    }
    
    @Test
    void getVehicleProfile_Exception() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\":\"\"}}", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfile(CLIENT_ID);
        });
    }
    
    @Test
    @Disabled("unable prepare data to mock")
    void getVehicleProfile_Empty() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Optional<VehicleProfile> val = vehicleProfileClient.getVehicleProfile(CLIENT_ID);
        
        assertTrue(val.isEmpty());
    }
    
    @Test
    void getVehicleProfileJson_Exception() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileJson(CLIENT_ID);
        });
    }
    
    @Test
    void updateVehicleProfile_Success() {
        when(restTemplate.patchForObject(anyString(), isA(VehicleProfile.class), eq(String.class)))
            .thenReturn("");
        
        VehicleProfile vp = new VehicleProfile();
        Optional<String> val = vehicleProfileClient.updateVehicleProfile("vehicle-id", vp, true);
        
        assertTrue(val.isPresent());
        assertEquals("", val.get());
    }
    
    @Test
    void updateVehicleProfile_Exception() {
        when(restTemplate.patchForObject(anyString(), isA(VehicleProfile.class), eq(String.class)))
            .thenThrow(new RestClientException("exception when patchForObject"));
        VehicleProfile vp = new VehicleProfile();
        try {
            vehicleProfileClient.updateVehicleProfile("vehicle-id", vp, false);
            Assertions.fail("Expected VehicleProfileException");
        } catch (VehicleProfileException e) {
            assertTrue(StringUtils.contains(e.getMessage(), "exception when patchForObject"));
        }
    }
    
    @Test
    void getVehicleProfileAttributes_Exception() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {}}", HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttributes("vehicle-id", false,
                VehicleProfileAttribute.VEHICLE_ID);
        });
    }
    
    @Test
    void getVehicleProfileAttributesAsObject_Success() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {\"ecus\":{\"hu\":{\"clientId\":\"client-id\"}}}}",
                HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Map<VehicleProfileAttribute, Optional<Object>> val =
            vehicleProfileClient.getVehicleProfileAttributesAsObject("vehicle-id", false,
                VehicleProfileAttribute.HU_CLIENT_ID);
        
        assertTrue(val.get(VehicleProfileAttribute.HU_CLIENT_ID).isPresent());
        assertEquals(CLIENT_ID, val.get(VehicleProfileAttribute.HU_CLIENT_ID).get());
    }
    
    @Test
    void getVehicleProfileAttributesAsObject_Exception() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {}}", HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttributesAsObject("vehicle-id", false,
                VehicleProfileAttribute.HU_CLIENT_ID);
        });
    }
    
    @Test
    void getVehicleProfileAttributesForDifferentType_boolean_Success() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {\"blockEnrollment\": false}}", HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Map<VehicleProfileAttribute, Optional<?>> map =
            vehicleProfileClient.getVehicleProfileAttributesForDifferentType("vehicle-id",
                false, VehicleProfileAttribute.BLOCK_ENROLLMENT);
        
        assertTrue(map.get(VehicleProfileAttribute.BLOCK_ENROLLMENT).isPresent());
        assertFalse((boolean) map.get(VehicleProfileAttribute.BLOCK_ENROLLMENT).get());
    }
    
    @Test
    void getVehicleProfileAttributesForDifferentType_boolean_Exception() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {}}", HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttributesForDifferentType("vehicle-id",
                false, VehicleProfileAttribute.BLOCK_ENROLLMENT);
        });
    }
    
    @Test
    void getVehicleProfileAttributesWithClientIdSuccess() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": [{\"vehicleAttributes\": {\"make\": \"ABCD\"}}]}",
                HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Map<VehicleProfileAttribute, Optional<String>> map =
            vehicleProfileClient.getVehicleProfileAttributesWithClientId(CLIENT_ID, false,
                VehicleProfileAttribute.MAKE);
        
        assertTrue(map.get(VehicleProfileAttribute.MAKE).isPresent());
        assertEquals("ABCD", map.get(VehicleProfileAttribute.MAKE).get());
    }
    
    @Test
    void getVehicleProfileAttributesWithClientIdIgnoreException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": [{\"vehicleAttributes\": {}}]}", HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Map<VehicleProfileAttribute, Optional<String>> map =
            vehicleProfileClient.getVehicleProfileAttributesWithClientId(CLIENT_ID, true,
                VehicleProfileAttribute.MAKE);
        
        assertTrue(map.get(VehicleProfileAttribute.MAKE).isEmpty());
    }
    
    @Test
    void getVehicleProfileAttributesWithClientIdException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": [{\"vehicleAttributes\": {}}]}", HttpStatus.ACCEPTED);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttributesWithClientId(CLIENT_ID, false,
                VehicleProfileAttribute.MAKE);
        });
    }
    
    @Test
    void getVehicleProfileAttributesWithClientIdApiException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": [{\"vehicleAttributes\": {}}]}", HttpStatus.NOT_FOUND);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttributesWithClientId(CLIENT_ID, false,
                VehicleProfileAttribute.MAKE);
        });
    }
    
    @Test
    void getVehicleProfileJsonWithClientId_Exception() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"data\": []}", HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileJsonWithClientId(CLIENT_ID);
        });
    }
    
    @Test
    void disassociatevehicleNotok() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": []}", HttpStatus.NOT_FOUND);
        
        when(restTemplate.exchange(isA(URI.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(DisassociationFailedException.class, () -> {
            vehicleProfileClient.disassociateVehicle("user-id", "vehicle-id");
        });
    }
    
    @Test
    void associatevehicleNotok() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": []}", HttpStatus.NOT_FOUND);
        
        when(restTemplate.exchange(isA(URI.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(AssociationFailedException.class, () -> {
            vehicleProfileClient.associateVehicle("user-id", "vehicle-id", "??");
        });
    }
    
    @Test
    @Disabled("unable prepare mock data")
    void isServiceProvisionedIoException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {\"ecus\":{}}}", HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertFalse(vehicleProfileClient.isServiceProvisioned("user-id", "service-id", "$.ecus"));
    }
    
    @Test
    void getVehicleProfileAttributesForVinSuccess() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {\"vehicleAttributes\":{\"modelYear\": \"2022\"}}}",
                HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Map<VehicleProfileAttribute, Optional<String>> map =
            vehicleProfileClient.getVehicleProfileAttributesForVin("vehicle-id", true,
                VehicleProfileAttribute.MODEL_YEAR);
        
        Optional<String> optModelYear = map.get(VehicleProfileAttribute.MODEL_YEAR);
        assertTrue(optModelYear.isPresent());
        assertEquals("2022", optModelYear.get());
    }
    
    @Test
    void getVehicleProfileAttributesForVinIgnoreException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {\"vehicleAttributes\":{}}}", HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        Map<VehicleProfileAttribute, Optional<String>> map =
            vehicleProfileClient.getVehicleProfileAttributesForVin("vehicle-id", true,
                VehicleProfileAttribute.MODEL_YEAR);
        assertTrue(map.get(VehicleProfileAttribute.MODEL_YEAR).isEmpty());
    }
    
    @Test
    void getVehicleProfileAttributesForVinException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": {\"vehicleAttributes\":{}}}", HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        assertThrows(RuntimeException.class, () -> {
            vehicleProfileClient.getVehicleProfileAttributesForVin("vehicle-id", false,
                VehicleProfileAttribute.MODEL_YEAR);
        });
    }
    
    @Test
    void getVehicleProfileAttributesWithClientIdOnDemandAttrSuccess() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": [{\"vehicleAttributes\":{\"ABCD\":\"ABCD_VALUE\"}}]}",
                HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        VehicleProfileOnDemandAttribute vehicleProfileOnDemandAttribute =
            new VehicleProfileOnDemandAttribute("ABCD", "$.data.vehicleAttributes.ABCD", String.class);
        Map<String, Optional<?>> map =
            vehicleProfileClient.getVehicleProfileAttributesWithClientId(CLIENT_ID, false,
                vehicleProfileOnDemandAttribute);
        
        Optional<?> opt = map.get(vehicleProfileOnDemandAttribute.getName());
        assertTrue(opt.isPresent());
        assertEquals("ABCD_VALUE", opt.get());
    }
    
    @Test
    void getVehicleProfileAttributesWithClientIdOnDemandAttrException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": [{\"vehicleAttributes\":{}}]}", HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        VehicleProfileOnDemandAttribute vehicleProfileOnDemandAttribute =
                new VehicleProfileOnDemandAttribute("ABCD", "$.data.vehicleAttributes.ABCD",
                        String.class);
        try {
            vehicleProfileClient.getVehicleProfileAttributesWithClientId(CLIENT_ID, false,
                    vehicleProfileOnDemandAttribute);
            Assertions.fail("Expected VehicleProfileException");
        } catch (VehicleProfileException e) {
            Assertions.assertTrue(() -> StringUtils.contains(e.getMessage(), "No results for path"));
        }
    }
    
    @Test
    void getVehicleProfileAttributesWithClientIdOnDemandAttrApiException() {
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"data\": [{\"vehicleAttributes\":{}}]}", HttpStatus.NOT_FOUND);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);

        VehicleProfileOnDemandAttribute vehicleProfileOnDemandAttribute =
                new VehicleProfileOnDemandAttribute("ABCD", "$.data.vehicleAttributes.ABCD",
                        String.class);
        try {
            vehicleProfileClient.getVehicleProfileAttributesWithClientId(CLIENT_ID, false,
                    vehicleProfileOnDemandAttribute);
            Assertions.fail("Expected VehicleProfileException");
        } catch (VehicleProfileException e) {
            Assertions.assertTrue(() ->
                    StringUtils.contains(e.getMessage(), "Failed to find vehicle profile for client id: client-id"));
        }
    }
    
    @Test
    void getVehicleProfileAttributes_OnDemandAttr_Success() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>(
            "{\"data\": {\"vehicleAttributes\":{\"ABCD\": true,\"EFG\": [\"EFG_VALUE\"]}}}",
            HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        VehicleProfileOnDemandAttribute attributeAbcd =
            new VehicleProfileOnDemandAttribute("ABCD", "$.data.vehicleAttributes.ABCD", Boolean.class);
        VehicleProfileOnDemandAttribute attributeEfg =
            new VehicleProfileOnDemandAttribute("EFG", "$.data.vehicleAttributes.EFG", HashSet.class);
        Map<String, Optional<?>> map =
            vehicleProfileClient.getVehicleProfileAttributes("vehicle-id", false, attributeAbcd,
                attributeEfg);
        assertTrue((Boolean) map.get(attributeAbcd.getName()).get());
        assertEquals("EFG_VALUE", ((HashSet) map.get(attributeEfg.getName()).get()).toArray()[0]);
    }
    
    @Test
    void getVehicleProfileAttributes_OnDemandAttr_Exception() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"data\": {}}", HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);

        VehicleProfileOnDemandAttribute attributeAbcd =
                new VehicleProfileOnDemandAttribute("ABCD", "$.data.vehicleAttributes.ABCD",
                        Boolean.class);
        try {
            vehicleProfileClient.getVehicleProfileAttributes("vehicle-id", false, attributeAbcd);
            Assertions.fail("Expected VehicleProfileException");
        } catch (VehicleProfileException e) {
            Assertions.assertTrue(() -> StringUtils.contains(e.getMessage(), "Missing property in path"));
        }
    }
    
    @Test
    void getVehicleProfileAttributes_OnDemandAttr_IgnoreException() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"data\": {}}", HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(responseEntity);
        
        VehicleProfileOnDemandAttribute attributeAbcd =
            new VehicleProfileOnDemandAttribute("ABCD", "$.data.vehicleAttributes.ABCD", Boolean.class);
        Map<String, Optional<?>> map =
            vehicleProfileClient.getVehicleProfileAttributes("vehicle-id", true, attributeAbcd);
        assertTrue(map.get(attributeAbcd.getName()).isEmpty());
    }
    
    @Test
    void testFetchAssociatedVehiclesForUser() {
        mockVehicleProfile();
        
        Object vpJson = JsonUtils.classpathToObject("/vehicleprofile/associatedVehiclesForUser.json");
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>(JsonUtils.toJsonString(vpJson), HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), org.mockito.ArgumentMatchers.eq(String.class)))
            .thenReturn(responseEntity);
        List<String> vins = vehicleProfileClient.getAssociatedVehicles("tushar");
        assertEquals(EXPECTED_30, vins.size());
    }
    
    @Test
    void testFetchWithNoVehiclesAssociated() {
        mockVehicleProfile();
        
        Object vpJson = JsonUtils.classpathToObject("/vehicleprofile/noAssociatedVehiclesForUser.json");
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>(JsonUtils.toJsonString(vpJson), HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), org.mockito.ArgumentMatchers.eq(String.class)))
            .thenReturn(responseEntity);
        List<String> vins = vehicleProfileClient.getAssociatedVehicles("tushar");
        assertEquals(0, vins.size());
    }
    
    @Test
    void testServiceProvisioned() {
        mockVehicleProfile();
        
        boolean val = vehicleProfileClient.isServiceProvisioned("19UYA31581L000004", "TANF",
            "$.ecus.['hu'].provisionedServices.['services'][*].serviceId");
        
        assertTrue(val);
    }
    
    @Test
    void testGetProvisionedServices() {
        mockVehicleProfile();
        
        Map<VehicleProfileAttribute, Optional<?>> vals =
            vehicleProfileClient.getVehicleProfileAttributesForDifferentType("foo", true,
                VehicleProfileAttribute.MSISDN,
                VehicleProfileAttribute.HU_PROVISIONED_SERVICES);
        
        Set huService = (Set) vals.get(VehicleProfileAttribute.HU_PROVISIONED_SERVICES).get();
        String msidn = (String) vals.get(VehicleProfileAttribute.MSISDN).get();
        assertEquals("12345", msidn);
        assertNotNull(huService);
        assertEquals(INT10, huService.size());
    }
    
    @Test
    void testGetVehicleAttrbsOnDemand() {
        mockVehicleProfile();
        
        VehicleProfileOnDemandAttribute attrMake =
            new VehicleProfileOnDemandAttribute("make", "$.data.vehicleAttributes.make",
                String.class);
        VehicleProfileOnDemandAttribute attrModel =
            new VehicleProfileOnDemandAttribute("model", "$.data.vehicleAttributes.model",
                String.class);
        VehicleProfileOnDemandAttribute attrUser =
            new VehicleProfileOnDemandAttribute("userId", "$.data.authorizedUsers[0].userId",
                String.class);
        
        Map<String, Optional<?>> vals =
            vehicleProfileClient.getVehicleProfileAttributes("19UYA31581L000004", true, attrMake,
                attrModel,
                attrUser);
        
        assertNotNull(vals.get("make"));
        assertNotNull(vals.get("model"));
        assertNotNull(vals.get("userId"));
        
        Map<String, Optional<?>> attrs =
            vehicleProfileClient.getVehicleProfileAttributesWithClientId("19UYA31581L000004", true,
                attrMake, attrModel, attrUser);
        
        assertNotNull(attrs.get("make"));
        assertNotNull(attrs.get("model"));
        assertNotNull(attrs.get("userId"));
    }
    
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {
    
    }
}
