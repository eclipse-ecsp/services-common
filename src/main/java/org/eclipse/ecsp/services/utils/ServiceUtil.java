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

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.AuthorizedPartner;
import org.eclipse.ecsp.domain.AuthorizedPartnerDetail;
import org.eclipse.ecsp.domain.AuthorizedPartnerDetailItem;
import org.eclipse.ecsp.domain.ServiceClaim;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.EventDataDeSerializer;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.eclipse.ecsp.domain.Constants.DTF_VEHICLE_PROFILE;
import static org.eclipse.ecsp.domain.Constants.USER_ID_UNKNOWN;

/**
 * Utils class that can be used across all the services.
 *
 * @author Neerajkumar
 */
@SuppressWarnings("java:S6857")
@Component
public class ServiceUtil {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ServiceUtil.class);
    
    @Value("${outboud.api.additional.headers:#{null}}")
    private String[] additionalHeaders;
    
    @Value("#{${outbound.api.headers.values.lookup:{:}}}")
    private Map<String, String> additionalHeadersWithTypes;

    private final VehicleProfileClient vehicleProfileClient;
    
    @Value("${vehicle.owner.role:VO}")
    private String vehicleOwnerRole;

    /**
     * Constructor for {@link ServiceUtil}.
     *
     * @param vehicleProfileClient the vehicle profile client
     */
    public ServiceUtil(VehicleProfileClient vehicleProfileClient) {
        this.vehicleProfileClient = vehicleProfileClient;
    }
    
    /**
     * Create standard ignite {@link ObjectMapper} with required filter and modules.
     *
     * @return instance of {@link ObjectMapper}
     */
    public static ObjectMapper createJsonMapperForIgniteEvent() {
        EventDataDeSerializer eventDataSerializer = new EventDataDeSerializer();
        SimpleModule module = new SimpleModule("PolymorphicEventDataModule",
            new com.fasterxml.jackson.core.Version(1, 0, 0, null, null, null));
        module.addDeserializer(EventData.class, eventDataSerializer);
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(module);
        jsonMapper.configure(Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        jsonMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        return jsonMapper;
    }
    
    /**
     * Create {@link IgniteEvent} with provide inputs.
     *
     * @param version   event version
     * @param eventId   event identifier
     * @param vehicleId vehicle identifier
     * @param eventData event data
     * @return instance of {@link IgniteEvent}
     */
    public static IgniteEventImpl createIgniteEvent(Version version, String eventId, String vehicleId,
                                                    EventData eventData) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(eventData);
        igniteEvent.setEventId(eventId);
        igniteEvent.setVersion(version);
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        return igniteEvent;
    }

    /**
     * checks if the provide resource file exists in the classpath.
     *
     * @param resourceFile file location.
     *
     * @return true if exists, otherwise false.
     */
    public static boolean doesResourceFileExist(String resourceFile) {
        return (ServiceUtil.class.getResource(resourceFile) != null);
    }

    /**
     * get header map.
     *
     * @param sessionId sessionId to be added to header map.
     * @param platformResponseId platformResponseId to be added to header map.
     *
     * @return map of headers containing sessionId and platformResponseId.
     */
    public Map<String, String> getHeaderMap(String sessionId, String platformResponseId) {
        return this.getHeaderMap(sessionId, platformResponseId, null);
    }
    
    /**
     * Prepare {@link Map} from the provided input.
     * and add additional headers from outboud.api.additional.headers property.
     *
     * @param sessionId          value of SessionId header
     * @param platformResponseId value of PlatformResponseId header
     * @param partnerId          value of PartnerId header
     * @return headers in {@link Map}
     */
    public Map<String, String> getHeaderMap(String sessionId, String platformResponseId,
                                            String partnerId) {
        Map<String, String> headerMap = new HashMap<>();
        if (null != additionalHeaders) {
            for (String header : additionalHeaders) {
                String[] arr = header.split(":");
                headerMap.put(arr[0], arr[1]);
            }
        }
        if (null != sessionId) {
            headerMap.put(EventAttribute.SESSION_ID, sessionId);
        }
        if (null != platformResponseId) {
            headerMap.put(EventAttribute.PLATFORM_RESPONSE_ID, platformResponseId);
        }
        if (null != partnerId) {
            headerMap.put(EventAttribute.PARTNER_ID, partnerId);
        }
        return headerMap;
    }

    /**
     * get header map for provided type.
     *
     * @param sessionId sessionId
     * @param platformResponseId platform
     * @param type add only particular additional header with provided type in headers.
     * @return map pf headers
     */
    public Map<String, String> getHeaderMapForTypes(String sessionId, String platformResponseId,
                                                    String type) {
        return this.getHeaderMapForTypes(sessionId, platformResponseId, type, null);
    }
    
    /**
     * Prepare {@link Map} from the provided input.
     * add additional headers from outbound.api.headers.values.lookup property.
     *
     * @param sessionId          value of SessionId header
     * @param platformResponseId value of PlatformResponseId header
     * @param type               add only particular additional header with provided type in headers
     * @param partnerId          value of PartnerId header
     * @return headers in {@link Map}
     */
    public Map<String, String> getHeaderMapForTypes(String sessionId, String platformResponseId,
                                                    String type, String partnerId) {
        Map<String, String> headerMap = new HashMap<>();
        if (null != additionalHeadersWithTypes && !additionalHeadersWithTypes.isEmpty()
            && null != additionalHeadersWithTypes.get(type)) {
            String[] headearr = additionalHeadersWithTypes.get(type).split(",");
            for (String headerData : headearr) {
                String[] arr = headerData.split(":");
                headerMap.put(arr[0], arr[1]);
            }
        }
        if (null != sessionId) {
            headerMap.put(EventAttribute.SESSION_ID, sessionId);
        }
        if (null != platformResponseId) {
            headerMap.put(EventAttribute.PLATFORM_RESPONSE_ID, platformResponseId);
        }
        if (null != partnerId) {
            headerMap.put(EventAttribute.PARTNER_ID, partnerId);
        }
        return headerMap;
    }
    
    /**
     * Calculate TTL based on provided input.
     * ((numberOfRetry * maxInterval) + bufferInterval + eventTimestamp)
     *
     * @param numberOfRetry  event retry count
     * @param maxInterval    internal till next retry
     * @param bufferInterval buffer time for the next retry interval
     * @param eventTimestamp event timestamp
     * @return calculated TTL, absolute timestamp
     */
    public long getEventTtl(long numberOfRetry, long maxInterval, long bufferInterval,
                            long eventTimestamp) {
        
        return ((numberOfRetry * maxInterval) + bufferInterval + eventTimestamp);
        
    }
    
    /**
     * Fetch user id from vehicle profile authorizedUsers and create UserContext.
     *
     * @param vehicleId vehicle identifier
     * @return list of {@link UserContext}
     */
    public List<UserContext> getUserContextInfo(String vehicleId) {
        Optional<String> optUserId =
            vehicleProfileClient.getVehicleProfileAttribute(vehicleId, VehicleProfileAttribute.USERID,
                true);
        
        String userId;
        if (optUserId.isPresent() && StringUtils.isNotEmpty(optUserId.get())) {
            userId = optUserId.get();
        } else {
            LOGGER.debug("unable get UserId from vehicleProfileClient, set to UNKNOWN");
            userId = USER_ID_UNKNOWN;
        }
        List<UserContext> userContextList = new ArrayList<>();
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        userContext.setRole(vehicleOwnerRole);
        userContextList.add(userContext);
        
        return userContextList;
    }

    /**
     * Fetch authorized partner details from vehicle profile.
     *
     * @param vehicleId  vehicle unique identifier
     * @param eventId    event id
     * @param serviceId  service id
     * @return authorized vehicle partner details
     */
    public AuthorizedPartnerDetail getAuthorizedPartnerDetail(String vehicleId, String eventId,
                                                              String serviceId) {
        return getAuthorizedPartnerDetail(vehicleId, eventId, new String[] {serviceId});
    }
    
    /**
     * Fetch authorized partner details from vehicle profile.
     *
     * @param vehicleId  vehicle unique identifier
     * @param eventId    event id
     * @param serviceIds service ids
     * @return authorized vehicle partner details
     */
    public AuthorizedPartnerDetail getAuthorizedPartnerDetail(String vehicleId, String eventId,
                                                              String[] serviceIds) {
        AuthorizedPartnerDetail authorizedPartnerDetail = new AuthorizedPartnerDetail();
        // get from vehicle profile
        Map<VehicleProfileAttribute, Optional<Object>> mapVehicleProfile =
            vehicleProfileClient.getVehicleProfileAttributesAsObject(vehicleId, true,
                VehicleProfileAttribute.BLOCK_ENROLLMENT, VehicleProfileAttribute.AUTHORIZED_PARTNERS,
                VehicleProfileAttribute.SOLD_REGION);
        
        //BLOCK_ENROLLMENT
        if (mapVehicleProfile.containsKey(VehicleProfileAttribute.BLOCK_ENROLLMENT)) {
            authorizedPartnerDetail.setChannelOutboundRequired(
                !getBlockEnrollment(mapVehicleProfile, vehicleId));
        }
        
        if (mapVehicleProfile.containsKey(VehicleProfileAttribute.SOLD_REGION)) {
            Optional<Object> soldRegion = mapVehicleProfile.get(VehicleProfileAttribute.SOLD_REGION);
            if (soldRegion.isPresent()) {
                authorizedPartnerDetail.setSoldRegion(String.valueOf(soldRegion.get()));
            }
        }
        
        // AUTHORIZED_PARTNERS
        if (mapVehicleProfile.containsKey(VehicleProfileAttribute.AUTHORIZED_PARTNERS)) {
            Optional<Object> optStrAuthorizedPartners =
                mapVehicleProfile.get(VehicleProfileAttribute.AUTHORIZED_PARTNERS);
            
            if (optStrAuthorizedPartners.isPresent()) {
                LinkedHashMap<String, AuthorizedPartner> data = (LinkedHashMap) optStrAuthorizedPartners.get();
                TypeReference<Map<String, AuthorizedPartner>> typeReference =
                    new TypeReference<Map<String, AuthorizedPartner>>() {
                    };
                
                try {
                    List<AuthorizedPartnerDetailItem> list =
                        JsonMapperUtils.getTypedObjectFromJson(data, typeReference).values().stream()
                            .filter(ap -> Arrays.stream(serviceIds).anyMatch(
                                serviceId -> isServiceClaimsValid(ap.getServiceClaims(), serviceId,
                                    LocalDateTime.now())))
                            .map(ap -> new AuthorizedPartnerDetailItem(ap.getPartnerId(),
                                "QUALIFIER_" + eventId + "_" + ap.getPartnerId()))
                            .toList();
                    
                    authorizedPartnerDetail.setOutboundDetails(list);
                    
                } catch (IllegalArgumentException e) {
                    LOGGER.error("get authorized partner detail json processing fail", e);
                }
            } else {
                LOGGER.debug("authorizedPartners not present. vehicleId: {}", vehicleId);
            }
        }
        
        return authorizedPartnerDetail;
    }
    
    private boolean getBlockEnrollment(
        Map<VehicleProfileAttribute, Optional<Object>> mapVehicleProfile, String vehicleId) {
        Optional<Object> optBlockEnrollment =
            mapVehicleProfile.get(VehicleProfileAttribute.BLOCK_ENROLLMENT);
        boolean blockEnrollment = false;
        if (optBlockEnrollment.isPresent()) {
            Object objBlockEnrollment = optBlockEnrollment.get();
            if (objBlockEnrollment instanceof Boolean) {
                blockEnrollment = (Boolean) optBlockEnrollment.get();
            } else if (objBlockEnrollment instanceof String) {
                blockEnrollment = Boolean.parseBoolean((String) optBlockEnrollment.get());
            }
        } else {
            LOGGER.debug("blockEnrollment not present, default to false. vehicleId: {}", vehicleId);
        }
        
        return blockEnrollment;
    }
    
    private boolean isServiceClaimsValid(Map<String, ServiceClaim> serviceClaimMap, String serviceId,
                                         LocalDateTime currentDt) {
        if (serviceClaimMap.containsKey(serviceId)) {
            ServiceClaim serviceClaim = serviceClaimMap.get(serviceId);
            
            String start = serviceClaim.getStart();
            String expire = serviceClaim.getExpire();
            
            LocalDateTime startDt = LocalDateTime.parse(start, DTF_VEHICLE_PROFILE);
            LocalDateTime expireDt = LocalDateTime.parse(expire, DTF_VEHICLE_PROFILE);
            
            boolean isValid = currentDt.isAfter(startDt) && currentDt.isBefore(expireDt);
            
            if (!isValid) {
                LOGGER.debug(
                    "current datetime NOT between start datetime and expire datetime. "
                        + "checkPointDt: {}, startDt: {}, expireDt: {}",
                    currentDt, startDt, expireDt);
            }
            
            return isValid;
        } else {
            LOGGER.debug("service id not exists in service claims, serviceId: {}", serviceId);
            
            return false;
        }
    }
}
