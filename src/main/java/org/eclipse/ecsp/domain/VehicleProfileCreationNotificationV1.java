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

package org.eclipse.ecsp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.entities.GenericEventData;

/**
 * Pojo for VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT ignite event.
 */
@EventMapping(id = Constants.EVENT_VEHICLE_CREATION_NOTIFICATION, version = Version.V1_1)
public class VehicleProfileCreationNotificationV1 extends GenericEventData {
    private static final long serialVersionUID = -1000632568157863585L;
    
    @Override
    public String toString() {
        ToStringBuilder vpcStringBuilder = new ToStringBuilder(this);
        vpcStringBuilder.append("data", getData());
        return vpcStringBuilder.toString();
    }
}
