/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.eclipse.ecsp.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Pojo to store vehicle profile.
 *
 * @author abhishekkumar
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class VehicleProfile {
    private String vin;
    private String vehicleId;
    private Date createdOn;
    private Date updatedOn;
    private Date productionDate;
    private String soldRegion;
    private String saleDate;
    private VehicleAttributes vehicleAttributes;
    private List<User> authorizedUsers;
    private ModemInfo modemInfo;
    private String vehicleArchType;
    private Map<String, ? extends Ecu> ecus;
    private Boolean dummy;
    private Map<String, Event> events;
    private Map<String, Map<String, String>> customParams;
    private VehicleCapabilities vehicleCapabilities;
    private SaleAttributes saleAttributes;
    private Boolean eolValidationInProgress;
    private Boolean blockEnrollment;
    private Map<String, ? extends AuthorizedPartner> authorizedPartners;
    private String epiddbChecksum;
    private String connectedPlatform;
}
