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

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.AssociatedVehicles;
import org.eclipse.ecsp.domain.VehicleProfile;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.entities.VehicleProfileOnDemandAttribute;
import org.eclipse.ecsp.services.exceptions.AssociationFailedException;
import org.eclipse.ecsp.services.exceptions.DisassociationFailedException;
import org.eclipse.ecsp.services.exceptions.VehicleProfileException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * utility for fetching vehicle profile details.
 *
 * @author abhishekkumar
 */
@ConditionalOnProperty(value = "vehicle.profile.client.enabled", havingValue = "true", matchIfMissing = true)
@Component(value = "scVehicleProfileClient")
public class VehicleProfileClient {
    private static final String FAILED_TO_ASSOCIATED_VEHICLE = "FAILED_TO_ASSOCIATED_VEHICLE";
    private static final String FAILED_TO_DISASSOCIATED_VEHICLE = "FAILED_TO_DISASSOCIATED_VEHICLE";
    private static final String STATUS = "status";
    private static final String DATA = "data";
    private static final String USER_ID = "userId";
    private static final String VEHICLE_ID = "vehicleId";
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(VehicleProfileClient.class);
    public static final String FETCHING_FOR_VEHICLE = "Fetching {} for vehicle: {}";
    public static final String ERROR_WHILE_QUERYING_JSON_PATH = "Error while querying json path {}: {}";
    public static final String FETCHING_VEHICLE_ATTRIBUTES_FOR_VEHICLE =
            "Fetching vehicle attributes: {} for vehicle: {}";
    public static final String DATA_PATH = "$.data";
    public static final String RESPONSE_FROM_VEHICLE_PROFILE_FOR_VEHICLE_ID =
            "Response from vehicle profile for vehicleId {}: {}";
    public static final String ERROR_WHILE_QUERYING_VEHICLE_JSON_PATH =
            "Error while querying vehicle: {}, json path {}: {}";
    public static final String FETCHED_VALUES_FOR_VEHICLE =
            "Fetched values: {} for vehicle: {}";
    public static final String ERROR_WHILE_QUERYING_VEHICLE_PROFILE_FOR_VEHICLE_ID =
            "Error while querying vehicle profile for vehicleId {}: {}";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate;
    
    @NotBlank
    @Value("${http.vp.url:localhost}")
    private String vehicleProfileEndPoint;
    
    @NotBlank
    @Value("${http.associated.vehicles.url:localhost}")
    private String associatedVehiclesEndpoint;
    
    @NotBlank
    @Value("${http.vehicles.url:localhost}")
    private String vehicleProfileClientIdEndPoint;
    
    //
    @NotBlank
    @Value("${http.disassociate.vehicle.url:localhost}")
    private String disassociateVehicleUrl;
    
    @NotBlank
    @Value("${http.associate.vehicle.url:localhost}")
    private String associateVehicleUrl;

    public VehicleProfileClient(@Qualifier("servicesCommonRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Fetch single vehicle profile attribute by vehicle client id.
     * the client id could be any ecus type client <br/>
     * which is supported by the vehicle profile in that region.
     *
     * @param clientId vehicle ecu client id
     * @param vpa      vehicle profile attribute name
     * @return vehicle profile attribute value
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Optional<String> getVehicleProfileAttrWithClientId(String clientId,
                                                              VehicleProfileAttribute vpa) {
        return getVehicleProfileAttrWithClientId(clientId, vpa, false);
    }
    
    /**
     * Fetch single vehicle profile attribute by vehicle client id.
     * the client id could be any ecus type client <br/>
     * which is supported by the vehicle profile in that region.
     *
     * @param clientId    vehicle ecu client id
     * @param vpa         vehicle profile attribute name
     * @param ignoreError if true the exception,errors will be suppressed
     * @return vehicle profile attribute value
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Optional<String> getVehicleProfileAttrWithClientId(String clientId,
                                                              VehicleProfileAttribute vpa,
                                                              boolean ignoreError) {
        LOGGER.debug(FETCHING_FOR_VEHICLE, vpa, clientId);
        String value = "";
        String responseJson = "";
        String jsonPath = "$.data[0].vehicleAttributes.name";
        try {
            responseJson = invokeVehicleProfileWithClientIdGetRestApi(clientId);
        } catch (Exception e) {
            LOGGER.debug("Error while querying vehicle profile for clientId {}: {}", clientId,
                e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        
        LOGGER.trace("Response from vehicle profile endpoint for clientId {}: {}", clientId,
            responseJson);
        
        try {
            
            value = JsonPath.read(responseJson, jsonPath);
        } catch (Exception e) {
            LOGGER.debug(ERROR_WHILE_QUERYING_JSON_PATH, vpa.getJsonPath(), e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        
        LOGGER.debug("Fetched ({}: {}) for vehicle: {}", vpa, value, clientId);
        return Optional.ofNullable(value);
    }
    
    /**
     * fetch single vehicle profile attribute. this internally calls vehicle profile api <br/>
     * and parse only required information for the response based on the provided attributes.
     *
     * @param vehicleId vehicle id for which details to be fetched.
     * @param vpa       vehicle profile attribute.
     * @return vehicle profile attribute value
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Optional<String> getVehicleProfileAttribute(String vehicleId,
                                                       VehicleProfileAttribute vpa) {
        return getVehicleProfileAttribute(vehicleId, vpa, false);
    }
    
    /**
     * fetch single vehicle profile attribute. this internally calls vehicle profile api <br/>
     * and parse only required information for the response based on the provided attributes.
     *
     * @param vehicleId   vehicle id for which details to be fetched.
     * @param vpa         vehicle profile attribute.
     * @param ignoreError if true the exception,errors will be suppressed
     * @return vehicle profile attribute value
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Optional<String> getVehicleProfileAttribute(String vehicleId, VehicleProfileAttribute vpa,
                                                       boolean ignoreError) {
        LOGGER.debug(FETCHING_FOR_VEHICLE, vpa, vehicleId);
        String value = "";
        String responseJson = invokeVehicleProfileGetRestApi(vehicleId, ignoreError);
        
        try {
            value = JsonPath.read(responseJson, vpa.getJsonPath());
        } catch (Exception e) {
            LOGGER.debug(ERROR_WHILE_QUERYING_JSON_PATH, vpa.getJsonPath(), e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        
        LOGGER.debug("Fetched ({}: {}) for vehicle: {}", vpa, value, vehicleId);
        return Optional.ofNullable(value);
    }
    
    /**
     * fetch multiple vehicle profile attributes. this internally calls vehicle profile api <br/>
     * and parse only required information for the response based on the provided attributes.
     *
     * @param vehicleId         vehicle id for which details to be fetched.
     * @param vehicleAttributes vehicle profile attributes.
     * @return Map of attributes with attribute name and its value.
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<VehicleProfileAttribute, Optional<String>> getVehicleProfileAttributes(
        String vehicleId,
        VehicleProfileAttribute... vehicleAttributes) {
        return getVehicleProfileAttributes(vehicleId, false, vehicleAttributes);
    }
    
    
    /**
     * this method going to be deprecated. use
     * #getVehicleProfileAttributesForDifferentType instead
     *
     * @param vehicleId         vehicle profile id
     * @param ignoreError       if true the exception,errors will be suppressed
     * @param vehicleAttributes vehicle profile attributes.
     * @return Map of attributes with attribute name and its value.
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<VehicleProfileAttribute, Optional<String>> getVehicleProfileAttributes(
        String vehicleId, boolean ignoreError,
        VehicleProfileAttribute... vehicleAttributes) {
        LOGGER.debug(FETCHING_FOR_VEHICLE, Arrays.toString(vehicleAttributes), vehicleId);
        String responseJson = invokeVehicleProfileGetRestApi(vehicleId, ignoreError);
        
        Map<VehicleProfileAttribute, Optional<String>> vehicleProfileVals =
            createvehicleProfileVals(ignoreError,
                responseJson, vehicleAttributes);
        
        LOGGER.debug(FETCHING_FOR_VEHICLE, vehicleProfileVals, vehicleId);
        return vehicleProfileVals;
    }
    
    /**
     * fetch vehicle attribute from vehicle profile.
     *
     * @param vehicleId         vehicle unique identifier
     * @param ignoreError       ignore all error/exception while fetching the vehicle attributes.
     * @param vehicleAttributes vehicle attributes to be fetched from vehicle profile
     *                          attribute contains
     *                          attribute name in the profile
     *                          path of the value in the profile and
     *                          type of the attribute data
     * @return Map of vehicle attributes
     */
    @SuppressWarnings("java:S1452")
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<String, Optional<?>> getVehicleProfileAttributes(String vehicleId,
                                                                boolean ignoreError,
                                                                VehicleProfileOnDemandAttribute... vehicleAttributes) {
        LOGGER.debug(FETCHING_VEHICLE_ATTRIBUTES_FOR_VEHICLE,
            Arrays.toString(vehicleAttributes), vehicleId);
        
        String responseJson = invokeVehicleProfileGetRestApi(vehicleId, ignoreError);
        
        Map<String, Optional<?>> vehicleProfileVals = new HashMap<>();
        for (VehicleProfileOnDemandAttribute v : vehicleAttributes) {
            try {
                Object jsonValue = JsonPath.read(responseJson, v.getJsonPath());
                switch (v.getType().getSimpleName()) {
                    case "String":
                        vehicleProfileVals.put(v.getName(), Optional.ofNullable((String) jsonValue));
                        break;
                    case "Boolean":
                        vehicleProfileVals.put(v.getName(),
                            Optional.of(
                                jsonValue == null ? Boolean.FALSE : Boolean.valueOf(jsonValue.toString())));
                        break;
                    case "HashSet":
                        if (jsonValue instanceof net.minidev.json.JSONArray jsonData) {
                            vehicleProfileVals.put(v.getName(), Optional.ofNullable(
                                    jsonData.stream().map(Object::toString)
                                    .collect(Collectors.toSet())));
                        }
                        break;
                    case "Object":
                    default:
                        vehicleProfileVals.put(v.getName(), Optional.ofNullable(jsonValue));
                        break;
                }
                
            } catch (Exception e) {
                LOGGER.debug(ERROR_WHILE_QUERYING_VEHICLE_JSON_PATH, vehicleId,
                    v.getJsonPath(), e.getMessage());
                if (!ignoreError) {
                    throw new VehicleProfileException(e);
                } else {
                    vehicleProfileVals.put(v.getName(), Optional.empty());
                }
            }
        }
        
        LOGGER.debug(FETCHED_VALUES_FOR_VEHICLE, vehicleProfileVals, vehicleId);
        return vehicleProfileVals;
    }
    
    /**
     * Fetch list of associated vehicle to the users.
     *
     * @param userId the user id for list of associated vehicle to be fetched
     * @return list of vehicle ids associated to the user
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public List<String> getAssociatedVehicles(String userId) {
        LOGGER.debug("Fetching list of associated vehicles for user: {}", userId);
        String responseJson = "";
        List<String> vins = new ArrayList<>();
        responseJson = invokeGetAssociatedVehiclesForUserApi(userId);
        if (!StringUtils.isEmpty(responseJson)) {
            LOGGER.debug("Response from vehicle profile endpoint for {}: {}", userId, responseJson);
            List<String> list = JsonPath.read(responseJson, "$.data[*].vehicleId");
            vins.addAll(list);
        }
        return vins;
    }
    
    /**
     * Update vehicle profile, internally calls vehicle profile's patch api.
     *
     * @param vehicleId   vehicle id for which the profile to be updated
     * @param vp          the payload of the update
     * @param ignoreError if true the exception,errors will be suppressed
     * @return response for the update request
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Optional<String> updateVehicleProfile(String vehicleId, VehicleProfile vp,
                                                 boolean ignoreError) {
        LOGGER.debug("Updating vehicle {} with {}", vehicleId, vp);
        String responseJson = "";
        try {
            responseJson = invokeVehicleProfilePatchRestApi(vehicleId, vp);
        } catch (Exception e) {
            LOGGER.debug(ERROR_WHILE_QUERYING_VEHICLE_PROFILE_FOR_VEHICLE_ID, vehicleId,
                e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        
        LOGGER.trace("Response from vehicle profile endpoint for {}: {}", vehicleId, responseJson);
        
        return Optional.ofNullable(responseJson);
    }
    
    /**
     * Fetch vehicle profile as {@link VehicleProfile}.
     *
     * @param vehicleId vehicle profile id
     * @return vehicle profile
     */
    public Optional<VehicleProfile> getVehicleProfile(String vehicleId) {
        Optional<String> vpJson = getVehicleProfileJson(vehicleId);
        if (vpJson.isPresent()) {
            try {
                return Optional.of(objectMapper.readValue(vpJson.get(), VehicleProfile.class));
            } catch (IOException e) {
                throw new VehicleProfileException(e);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Fetch vehicle profile raw json as string.
     *
     * @param vehicleId vehicle profile id
     * @return raw json as string
     */
    public Optional<String> getVehicleProfileJson(String vehicleId) {
        String vpJson = null;
        String vpJsonGetResp = invokeVehicleProfileGetRestApi(vehicleId, false);
        try {
            vpJson = objectMapper.writeValueAsString(JsonPath.read(vpJsonGetResp, DATA_PATH));
        } catch (Exception e) {
            LOGGER.debug(ERROR_WHILE_QUERYING_JSON_PATH, DATA_PATH, e.getMessage());
            throw new VehicleProfileException(e);
        }
        return Optional.ofNullable(vpJson);
    }
    
    private String invokeVehicleProfilePatchRestApi(String vehicleId, VehicleProfile vp) {
        String vehicleProfilePatchPath = UriComponentsBuilder.fromUriString(vehicleProfileEndPoint)
            .path(vehicleId).toUriString();
        return restTemplate.patchForObject(vehicleProfilePatchPath, vp, String.class);
    }
    
    
    /**
     * this method going to be deprecated. use
     * #getVehicleProfileAttributesForDifferentType instead
     *
     * @param vehicleId         vehicle profile id
     * @param ignoreError       if true the exception,errors will be suppressed
     * @param vehicleAttributes vehicle profile attributes.
     * @return Map of attributes with attribute name and its value.
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<VehicleProfileAttribute, Optional<Object>> getVehicleProfileAttributesAsObject(
        String vehicleId, boolean ignoreError,
        VehicleProfileAttribute... vehicleAttributes) {
        LOGGER.debug(FETCHING_VEHICLE_ATTRIBUTES_FOR_VEHICLE,
            Arrays.toString(vehicleAttributes), vehicleId);
        String responseJson = invokeVehicleProfileGetRestApi(vehicleId, ignoreError);
        
        Map<VehicleProfileAttribute, Optional<Object>> vehicleProfileVals =
            new EnumMap<>(VehicleProfileAttribute.class);
        for (VehicleProfileAttribute v : vehicleAttributes) {
            try {
                Object jsonValue = JsonPath.read(responseJson, v.getJsonPath());
                vehicleProfileVals.put(v, Optional.ofNullable(jsonValue));
            } catch (Exception e) {
                LOGGER.debug(ERROR_WHILE_QUERYING_VEHICLE_JSON_PATH, vehicleId,
                    v.getJsonPath(), e.getMessage());
                if (!ignoreError) {
                    throw new VehicleProfileException(e);
                } else {
                    vehicleProfileVals.put(v, Optional.empty());
                }
            }
        }
        
        LOGGER.debug(FETCHED_VALUES_FOR_VEHICLE, vehicleProfileVals, vehicleId);
        return vehicleProfileVals;
    }
    
    /**
     * fetch multiple vehicle profile attributes with different data type. <br/>
     * this internally calls vehicle profile api <br/>
     * and parse only required information for the response based on the provided attributes.
     *
     * @param vehicleId         vehicle id for which details to be fetched.
     * @param ignoreError       if true the exception,errors will be suppressed
     * @param vehicleAttributes vehicle profile attributes.
     * @return Map of attributes with attribute name and its different data type and value.
     */
    @SuppressWarnings("java:S1452")
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<VehicleProfileAttribute, Optional<?>> getVehicleProfileAttributesForDifferentType(
        String vehicleId, boolean ignoreError,
        VehicleProfileAttribute... vehicleAttributes) {
        LOGGER.debug(FETCHING_VEHICLE_ATTRIBUTES_FOR_VEHICLE,
            Arrays.toString(vehicleAttributes), vehicleId);
        
        String responseJson = invokeVehicleProfileGetRestApi(vehicleId, ignoreError);
        
        Map<VehicleProfileAttribute, Optional<?>> vehicleProfileVals =
            new EnumMap<>(VehicleProfileAttribute.class);
        for (VehicleProfileAttribute v : vehicleAttributes) {
            try {
                Object jsonValue = JsonPath.read(responseJson, v.getJsonPath());
                switch (v.getType().getSimpleName()) {
                    case "String":
                        vehicleProfileVals.put(v, Optional.ofNullable((String) jsonValue));
                        break;
                    case "Boolean":
                        vehicleProfileVals.put(v, Optional.ofNullable(
                            jsonValue == null ? Boolean.FALSE : Boolean.valueOf(jsonValue.toString())));
                        break;
                    case "HashSet":
                        if (jsonValue instanceof net.minidev.json.JSONArray jsonData) {
                            vehicleProfileVals.put(v, Optional.ofNullable(
                                jsonData.stream().map(Object::toString)
                                    .collect(Collectors.toSet())));
                        }
                        break;
                    case "Object":
                    default:
                        vehicleProfileVals.put(v, Optional.ofNullable(jsonValue));
                        break;
                }
                
            } catch (Exception e) {
                LOGGER.debug(ERROR_WHILE_QUERYING_VEHICLE_JSON_PATH, vehicleId,
                    v.getJsonPath(), e.getMessage());
                if (!ignoreError) {
                    throw new VehicleProfileException(e);
                } else {
                    vehicleProfileVals.put(v, Optional.empty());
                }
            }
        }
        
        LOGGER.debug(FETCHED_VALUES_FOR_VEHICLE, vehicleProfileVals, vehicleId);
        return vehicleProfileVals;
    }
    
    private String invokeVehicleProfileGetRestApi(String vehicleId, boolean ignoreError) {
        String result = "";
        String vehicleProfileGetPath = UriComponentsBuilder.fromUriString(vehicleProfileEndPoint)
            .path(vehicleId).toUriString();
        try {
            ResponseEntity<String> response =
                restTemplate.getForEntity(vehicleProfileGetPath, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                result = response.getBody();
            }
        } catch (Exception e) {
            LOGGER.debug(ERROR_WHILE_QUERYING_VEHICLE_PROFILE_FOR_VEHICLE_ID, vehicleId,
                e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        LOGGER.debug(RESPONSE_FROM_VEHICLE_PROFILE_FOR_VEHICLE_ID, vehicleId, result);
        return result;
    }
    
    private String invokeGetAssociatedVehiclesForUserApi(String userId) {
        
        Map<String, String> params = new HashMap<>();
        params.put(EventAttribute.USERID, userId);
        
        String associatedVehiclesGetPath = UriComponentsBuilder.fromUriString(associatedVehiclesEndpoint)
            .buildAndExpand(params)
            .toUriString();
        LOGGER.debug("Hitting vehicle profile to get associated vehicles for user: {} and path is:{}",
            userId, associatedVehiclesGetPath);
        ResponseEntity<String> response =
            restTemplate.getForEntity(associatedVehiclesGetPath, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        throw new VehicleProfileException("Failed to find associated vehicles for the user: {}" + userId);
    }
    
    /**
     * Fetch associated vehicles for user id.
     *
     * @param userId user id for which the profile is associated.
     * @return associated vehicle profiles
     */
    public AssociatedVehicles getAssociatedVehiclesForUser(String userId) {
        URI associatedVehiclesGetPath = UriComponentsBuilder.fromUriString(associatedVehiclesEndpoint)
            .buildAndExpand(Collections.singletonMap(USER_ID, userId))
            .toUri();
        LOGGER.debug("retrieving associated vehicles, endpoint:{}", associatedVehiclesGetPath);
        ResponseEntity<AssociatedVehicles> response =
            restTemplate.getForEntity(associatedVehiclesGetPath, AssociatedVehicles.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        throw new VehicleProfileException("Failed to retrieve associated vehicles for the user: {}" + userId);
    }
    
    private String invokeVehicleProfileWithClientIdGetRestApi(String clientId) {
        String vehicleProfileGetPath = UriComponentsBuilder.fromUriString(vehicleProfileClientIdEndPoint)
            .queryParam(EventAttribute.CLIENTID, clientId)
            .toUriString();
        
        LOGGER.debug("invokeVehicleProfileWithClientIdGetRestApi request url: {}",
            vehicleProfileGetPath);
        
        ResponseEntity<String> response =
            restTemplate.getForEntity(vehicleProfileGetPath, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String body = response.getBody();
            LOGGER.debug("invokeVehicleProfileWithClientIdGetRestApi response payload: {}", body);
            return body;
        }
        throw new VehicleProfileException("Failed to find vehicle profile for client id: " + clientId);
    }
    
    /**
     * fetch vehicle attribute using vehicle clientId
     * client id could be any supported ecus.
     *
     * @param vehicleId         vehicle unique identifier
     * @param ignoreError       ignore all error/exception while fetching the vehicle attributes.
     * @param vehicleAttributes vehicle attributes to be fetched from vehicle profile
     *                          attribute contains
     *                          attribute name in the profile
     *                          path of the value in the profile and
     *                          type of the attribute data
     * @return Map of vehicle attributes
     */
    @SuppressWarnings("java:S1452")
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<String, Optional<?>> getVehicleProfileAttributesWithClientId(String vehicleId,
           boolean ignoreError,
           VehicleProfileOnDemandAttribute... vehicleAttributes) {
        LOGGER.debug(FETCHING_FOR_VEHICLE, Arrays.toString(vehicleAttributes), vehicleId);
        String responseJson = "";
        try {
            responseJson = invokeVehicleProfileWithClientIdGetRestApi(vehicleId);
        } catch (Exception e) {
            LOGGER.debug(ERROR_WHILE_QUERYING_VEHICLE_PROFILE_FOR_VEHICLE_ID, vehicleId,
                e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        
        LOGGER.trace(RESPONSE_FROM_VEHICLE_PROFILE_FOR_VEHICLE_ID, vehicleId, responseJson);
        
        Map<String, Optional<?>> vehicleProfileVals = new HashMap<>();
        for (VehicleProfileOnDemandAttribute v : vehicleAttributes) {
            try {
                JSONObject data = (JSONObject) new JSONObject(responseJson).getJSONArray(DATA).get(0);
                JSONObject dataObject = new JSONObject().put(DATA, data);
                String jsonValue = JsonPath.read(dataObject.toString(), v.getJsonPath());
                vehicleProfileVals.put(v.getName(), Optional.ofNullable(jsonValue));
            } catch (Exception e) {
                LOGGER.debug(ERROR_WHILE_QUERYING_JSON_PATH, v.getJsonPath(), e.getMessage());
                if (!ignoreError) {
                    throw new VehicleProfileException(e);
                } else {
                    vehicleProfileVals.put(v.getName(), Optional.empty());
                }
            }
        }
        
        LOGGER.debug(FETCHED_VALUES_FOR_VEHICLE, vehicleProfileVals, vehicleId);
        return vehicleProfileVals;
    }
    
    /**
     * Fetch single vehicle profile attribute by vehicle client id.
     * the client id could be any ecus type client <br/>
     * which is supported by the vehicle profile in that region.
     *
     * @param vehicleId         vehicle id for which details to be fetched.
     * @param ignoreError       if true the exception,errors will be suppressed
     * @param vehicleAttributes vehicle profile attributes.
     * @return Map of attributes with attribute name and its value.
     */
    public Map<VehicleProfileAttribute, Optional<String>> getVehicleProfileAttributesWithClientId(
        String vehicleId,
        boolean ignoreError, VehicleProfileAttribute... vehicleAttributes) {
        LOGGER.debug(FETCHING_FOR_VEHICLE, Arrays.toString(vehicleAttributes), vehicleId);
        String responseJson = "";
        try {
            responseJson = invokeVehicleProfileWithClientIdGetRestApi(vehicleId);
        } catch (Exception e) {
            LOGGER.debug("Error while querying vehicle profile for vechileId {}: {}", vehicleId,
                e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        
        LOGGER.trace(RESPONSE_FROM_VEHICLE_PROFILE_FOR_VEHICLE_ID, vehicleId, responseJson);
        
        Map<VehicleProfileAttribute, Optional<String>> vehicleProfileVals =
            new EnumMap<>(VehicleProfileAttribute.class);
        for (VehicleProfileAttribute v : vehicleAttributes) {
            try {
                JSONObject data = (JSONObject) new JSONObject(responseJson).getJSONArray(DATA).get(0);
                JSONObject dataObject = new JSONObject().put(DATA, data);
                String jsonValue = JsonPath.read(dataObject.toString(), v.getJsonPath());
                vehicleProfileVals.put(v, Optional.ofNullable(jsonValue));
            } catch (Exception e) {
                LOGGER.debug(ERROR_WHILE_QUERYING_JSON_PATH, v.getJsonPath(), e.getMessage());
                if (!ignoreError) {
                    throw new VehicleProfileException(e);
                } else {
                    vehicleProfileVals.put(v, Optional.empty());
                }
            }
        }
        
        LOGGER.debug(FETCHED_VALUES_FOR_VEHICLE, vehicleProfileVals, vehicleId);
        return vehicleProfileVals;
    }
    
    /**
     * Fetch raw vehicle profile by vehicle's client id.
     * the client id could be any ecus type client <br/>
     * which is supported by the vehicle profile in that region.
     *
     * @param clientId client id for which details to be fetched.
     * @return Map of attributes with attribute name and its value.
     */
    public Optional<String> getVehicleProfileJsonWithClientId(String clientId) {
        String vpJson = null;
        
        LOGGER.debug("getVehicleProfileJsonWithClientId clientId: {}", clientId);
        
        String vpJsonGetResp = invokeVehicleProfileWithClientIdGetRestApi(clientId);
        
        LOGGER.debug("getVehicleProfileJsonWithClientId vpJsonGetResp: {}", vpJsonGetResp);
        
        try {
            vpJson = objectMapper.writeValueAsString(JsonPath.read(vpJsonGetResp, "$.data[0]"));
        } catch (Exception e) {
            LOGGER.debug(ERROR_WHILE_QUERYING_JSON_PATH, DATA_PATH, e.getMessage());
            throw new VehicleProfileException(e);
        }
        return Optional.ofNullable(vpJson);
    }
    
    
    /**
     * Disassociate user from the vehicle profile.
     *
     * @param userId    currently associated userId.
     * @param vehicleId vehicle unique identifier
     * @return true if disassociated successfully
     * @throws DisassociationFailedException if any error occurred during disassociation.
     */
    public boolean disassociateVehicle(String userId, String vehicleId)
        throws DisassociationFailedException {
        URI disassociateVehiclePath = UriComponentsBuilder.fromUriString(disassociateVehicleUrl)
            .buildAndExpand(Collections.singletonMap(VEHICLE_ID, vehicleId))
            .toUri();
        LOGGER.debug("disassociating vehicle, endpoint:{}", disassociateVehiclePath);
        Map<String, String> data = Collections.singletonMap(USER_ID, userId);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(data);
        ResponseEntity<String> response =
            restTemplate.exchange(disassociateVehiclePath, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            Boolean isDisassociated = JsonPath.read(response.getBody(), DATA);
            if (Boolean.TRUE.equals(isDisassociated)) {
                LOGGER.info("disassociated vehicle:{} from user:{}", vehicleId, userId);
                return true;
            }
        }
        LOGGER.error("Failed to disassociating vehicle:{} from user:{},http status code:{}", vehicleId,
            userId, response.getStatusCode());
        throw new DisassociationFailedException(FAILED_TO_DISASSOCIATED_VEHICLE,
            String.format("Failed to disassociating vehicle:%s from user:%s", vehicleId, userId));
    }
    
    /**
     * Associate user to vehicle profile.
     *
     * @param userId    user to be associated
     * @param vehicleId vehicle unique identifier
     * @param status    to be sent while associated
     * @return true if associated successfully
     * @throws AssociationFailedException if any error occurred during association
     */
    public boolean associateVehicle(String userId, String vehicleId, String status)
        throws AssociationFailedException {
        URI associateVehiclePath = UriComponentsBuilder.fromUriString(associateVehicleUrl)
            .buildAndExpand(Collections.singletonMap(VEHICLE_ID, vehicleId))
            .toUri();
        LOGGER.debug("associating vehicle, endpoint:{}", associateVehiclePath);
        Map<String, String> data = new HashMap<>();
        data.put(USER_ID, userId);
        data.put(STATUS, status);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(data);
        ResponseEntity<String> response =
            restTemplate.exchange(associateVehiclePath, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.debug("associated vehicle:{} with user:{}", vehicleId, userId);
            return JsonPath.read(response.getBody(), DATA);
        }
        LOGGER.error("Failed to associating vehicle:{} with user:{},http status code:{}", vehicleId,
            userId, response.getStatusCode());
        throw new AssociationFailedException(FAILED_TO_ASSOCIATED_VEHICLE,
            String.format("Failed to associating vehicle:%s with user:%s", vehicleId, userId));
    }
    
    /**
     * Validate if the requested service id is provisioned in vehicle profile.
     *
     * @param vehicleId vehicle unique identifier
     * @param serviceId service id to be validated in vehicle profile
     * @param jsonPath  path of the service id in the vehicle profile
     * @return true if the service id is provisioned in vehicle profile
     */
    public boolean isServiceProvisioned(String vehicleId, String serviceId, String jsonPath) {
        try {
            Optional<String> vpJson = this.getVehicleProfileJson(vehicleId);
            LOGGER.debug("Response from vehicle profile endpoint for {} : {}", vehicleId, vpJson);
            
            if (vpJson.isPresent()) {
                String value = objectMapper.writeValueAsString(JsonPath.read(vpJson.get(),
                    jsonPath));
                return value.contains(serviceId);
            }
            
        } catch (IOException e) {
            LOGGER.error("Exception while fetching vehicle profile", e);
        }
        return false;
    }
    
    /**
     * Fetch requested vehicle attribute from the vehicle profile.
     *
     * @param vin               vehicle unique identifier
     * @param ignoreError       ignore all error/exception while fetching the vehicle attributes.
     * @param vehicleAttributes vehicle attributes to be fetched from vehicle profile
     * @return Map of vehicle attributes
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<VehicleProfileAttribute, Optional<String>> getVehicleProfileAttributesForVin(
        String vin, boolean ignoreError,
        VehicleProfileAttribute... vehicleAttributes) {
        LOGGER.debug(FETCHING_FOR_VEHICLE, Arrays.toString(vehicleAttributes), vin);
        String responseJson = invokeVehicleProfileGetRestApiForVin(vin, ignoreError);
        
        Map<VehicleProfileAttribute, Optional<String>> vehicleProfileVals =
            createvehicleProfileVals(ignoreError,
                responseJson, vehicleAttributes);
        
        LOGGER.debug(FETCHED_VALUES_FOR_VEHICLE, vehicleProfileVals, vin);
        return vehicleProfileVals;
    }
    
    private String invokeVehicleProfileGetRestApiForVin(String vin, boolean ignoreError) {
        String result = "";
        String vehicleProfileGetPath =
            UriComponentsBuilder.fromUriString(vehicleProfileEndPoint).queryParam("vin", vin)
                .toUriString();
        try {
            ResponseEntity<String> response =
                restTemplate.getForEntity(vehicleProfileGetPath, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                result = response.getBody();
            }
        } catch (Exception e) {
            LOGGER.debug("Error while querying vehicle profile for vechileId {}: {}", vin,
                e.getMessage());
            if (!ignoreError) {
                throw new VehicleProfileException(e);
            }
        }
        LOGGER.debug(RESPONSE_FROM_VEHICLE_PROFILE_FOR_VEHICLE_ID, vin, result);
        return result;
    }
    
    private Map<VehicleProfileAttribute, Optional<String>> createvehicleProfileVals(
        boolean ignoreError,
        String responseJson, VehicleProfileAttribute... vehicleAttributes) {
        Map<VehicleProfileAttribute, Optional<String>> vehicleProfileVals =
            new EnumMap<>(VehicleProfileAttribute.class);
        for (VehicleProfileAttribute v : vehicleAttributes) {
            try {
                String jsonValue = JsonPath.read(responseJson, v.getJsonPath());
                vehicleProfileVals.put(v, Optional.ofNullable(jsonValue));
            } catch (Exception e) {
                LOGGER.debug(ERROR_WHILE_QUERYING_JSON_PATH, v.getJsonPath(), e.getMessage());
                if (!ignoreError) {
                    throw new VehicleProfileException(e);
                } else {
                    vehicleProfileVals.put(v, Optional.empty());
                }
            }
        }
        return vehicleProfileVals;
    }
}