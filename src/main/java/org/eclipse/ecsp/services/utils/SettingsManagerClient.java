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
import com.jayway.jsonpath.JsonPath;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.ecsp.services.exceptions.SettingsMgmtException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Setting management client for interacting with setting-mgmt microservices.
 */
@ConditionalOnProperty(value = "settings.manager.client.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class SettingsManagerClient {
    
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(SettingsManagerClient.class);

    private final RestTemplate restTemplate;
    
    @NotBlank
    @Value("${http.sm.url:localhost}")
    private String settingsManagerEndPoint;

    /**
     * Constructor for {@link SettingsManagerClient}.
     *
     * @param restTemplate the rest template
     */
    public SettingsManagerClient(@Qualifier("servicesCommonRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Fetch settings management configuration for a vehicle.
     *
     * @param userId    associated user to the vehicle
     * @param vehicleId unique identifier of the vehicle.
     * @param settingId unique identifier of the vehicle setting
     * @param configKey setting key in the setting management config
     * @return requested config based on the configKey
     */
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    public Map<String, Object> getSettingsManagerConfigurationObject(String userId, String vehicleId,
                                                                     String settingId,
                                                                     String configKey) {
        LOGGER.debug("Fetching settings manager record for vehicle: {}", vehicleId);
        String responseJson = "";
        Map<String, Object> valueMap = null;
        try {
            responseJson = invokeSettingsManagerGetRestApi(userId, vehicleId, settingId);
        } catch (Exception e) {
            LOGGER.debug("Error while querying vehicle profile for vechileId {}: {}", vehicleId,
                e.getMessage());
        }
        
        LOGGER.trace("Response from settings manager endpoint for {}: {}", vehicleId, responseJson);
        try {
            valueMap = JsonPath.read(responseJson, configKey);
            LOGGER.debug("ConfigurationObject is :{}", valueMap);
        } catch (Exception e) {
            LOGGER.debug("Error while querying json path {}: {}",
                configKey, e.getMessage());
        }
        
        return valueMap;
    }
    
    private String invokeSettingsManagerGetRestApi(String userId, String vehicleId,
                                                   String settingId) {
        
        LOGGER.debug(
            "inside invokeSettingsManagerGetRestApi parameters"
                + " received  userId:{} vehicleId:{} settingId:{} ",
            userId, vehicleId,
            settingId);
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("vehicleId", vehicleId);
        
        URI uri = UriComponentsBuilder.fromUriString(settingsManagerEndPoint)
            .buildAndExpand(params)
            .toUri();
        
        String settingsManagerGetPath = UriComponentsBuilder
            .fromUri(uri)
            .queryParam("settingId", settingId)
            .build()
            .toUriString();
        
        LOGGER.debug("Invoking Settings Manager API with the following URL :{}",
            settingsManagerGetPath);
        
        ResponseEntity<String> response =
            restTemplate.getForEntity(settingsManagerGetPath, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.debug("Response received is :{}", response);
            LOGGER.debug("Response Body received is :{}", response.getBody());
            return response.getBody();
        }
        throw new SettingsMgmtException(
            "Failed to find settings manager record for vehicle id: " + vehicleId);
    }
    
}
