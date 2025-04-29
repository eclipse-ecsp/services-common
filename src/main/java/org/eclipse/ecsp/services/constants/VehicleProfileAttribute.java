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

package org.eclipse.ecsp.services.constants;

import lombok.Getter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vehicle profile enums with profile json path.<br/>
 * used to fetch useful information from vehicle profile json response.
 */
public enum VehicleProfileAttribute {
    /**
     * vehicle make (brand).
     */
    MAKE("make", "$.data.vehicleAttributes.make", String.class),
    /**
     * vehicle model.
     */
    MODEL("model", "$.data.vehicleAttributes.model", String.class),
    /**
     * vehicle authorized user.
     */
    USERID("userId", "$.data.authorizedUsers[0].userId", String.class),
    /**
     * modem msisdn.
     */
    MSISDN("msisdn", "$.data.modemInfo.msisdn", String.class),
    /**
     * vehicle identifier.
     */
    VIN("vin", "$.data.vin", String.class),
    /**
     * vehicle id.
     */
    VEHICLE_ID("vehicleId", "$.data.vehicleId", String.class),
    /**
     * vehicle name, set by user.
     */
    NAME("name", "$.data.vehicleAttributes.name", String.class),
    /**
     * vehicle model year.
     */
    MODEL_YEAR("modelYear", "$.data.vehicleAttributes.modelYear", String.class),
    /**
     * vehicle country.
     */
    DESTINATION_COUNTRY("destinationCountry", "$.data.vehicleAttributes.destinationCountry",
        String.class),
    /**
     * vehicle licence plate.
     */
    LICENSE_PLATE("licensePlate", "$.data.authorizedUsers[0].licensePlate", String.class),
    /**
     * vehicle sold region.
     */
    SOLD_REGION("soldRegion", "$.data.soldRegion", String.class),
    /**
     * vehicle block enrollment.
     */
    BLOCK_ENROLLMENT("blockEnrollment", "$.data.blockEnrollment", Boolean.class),
    /**
     * vehicle authorized partners.
     */
    AUTHORIZED_PARTNERS("authorizedPartners", "$.data.authorizedPartners", String.class),
    /**
     * services provisioned for hu ecu.
     */
    HU_PROVISIONED_SERVICES("huProvisionedServices",
        "$.data.ecus.hu.provisionedServices.['services'][*].serviceId",
        HashSet.class),
    /**
     * vehicle authorized users.
     */
    AUTHORIZED_USERS("authorizedUsers", "$.data.authorizedUsers", List.class),
    /**
     * hu ecu clientId.
     */
    HU_CLIENT_ID("huClientId", "$.data.ecus.hu.clientId", String.class),
    /**
     * vehicle model code.
     */
    MODEL_CODE("modelCode", "$.data.vehicleAttributes.modelCode", String.class),
    /**
     * modem imei.
     */
    IMEI("imei", "$.data.modemInfo.imei", String.class),
    /**
     * vehicle imsi.
     */
    IMSI("imsi", "$.data.modemInfo.imsi", String.class),
    /**
     * vehicle hu ecu type.
     */
    HU_ECUTYPE("huEcuType", "$.data.ecus.hu.ecuType", String.class),
    /**
     * deviceMgmtApproach.
     */
    DEVICE_MGMT_APPROACH("deviceMgmtApproach", "$.data.deviceMgmtApproach", String.class),
    /**
     * vehicle hu ecu serial number.
     */
    HU_SERIAL_NO("huSerialNo", "$.data.ecus.hu.serialNo", String.class);

    
    private static final Map<VehicleProfileAttribute, String> VEHICLE_PROFILE_ATTRIBUTE_PATH_MAP =
        Arrays.stream(VehicleProfileAttribute.values())
            .collect(Collectors.toMap(v -> v, v -> v.getJsonPath()));
    /**
     * -- GETTER --
     *  get json path.
     *
     * @return json path
     */
    @Getter
    private final String jsonPath;
    private final String name;
    @Getter
    private final Class<?> type;

    /**
     * initialize VehicleProfileAttribute.
     *
     * @param name name of the attribute.
     * @param jsonPath path of the attribute in vehicle profile.
     * @param type attribute type.
     */
    VehicleProfileAttribute(String name, String jsonPath, Class<?> type) {
        this.name = name;
        this.jsonPath = jsonPath;
        this.type = type;
    }

    /**
     * get attribute path.
     *
     * @param vpa vehicle profile attribute.
     *
     * @return attribute path.
     */
    public static String getJsonPath(VehicleProfileAttribute vpa) {
        return VEHICLE_PROFILE_ATTRIBUTE_PATH_MAP.get(vpa);
    }

    @Override
    public String toString() {
        return name;
    }
}
