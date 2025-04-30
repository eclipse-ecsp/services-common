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

import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import java.util.Map;

/**
 * DeviceMessagingAgent (DMA) response pojo.
 */
public class DmaResponse {
    
    /**
     * Possible response status from DMA.
     */
    public enum Response {
        /**
         * SUCCESS.
         */
        SUCCESS(Constants.SUCCESS),
        /**
         * FAIL.
         */
        FAIL(Constants.FAIL),
        /**
         * FAIL_MESSAGE_DELIVERY_TIMED_OUT.
         */
        FAIL_MESSAGE_DELIVERY_TIMED_OUT(Constants.FAIL_MESSAGE_DELIVERY_TIMED_OUT),
        /**
         * FAIL_VEHICLE_NOT_CONNECTED.
         */
        FAIL_VEHICLE_NOT_CONNECTED(Constants.FAIL_VEHICLE_NOT_CONNECTED),
        /**
         * FAIL_DELIVERY_RETRYING.
         */
        FAIL_DELIVERY_RETRYING(Constants.FAIL_DELIVERY_RETRYING),
        /**
         * RETRYING_DEVICE_DELIVERY_MESSAGE.
         */
        RETRYING_DEVICE_DELIVERY_MESSAGE(Constants.RETRYING_DEVICE_DELIVERY_MESSAGE),
        /**
         * RETRYING_AUTOMATICALLY_FOR_7_DAYS.
         */
        RETRYING_AUTOMATICALLY_FOR_7_DAYS(Constants.RETRYING_AUTOMATICALLY_FOR_7_DAYS);
        
        private final String value;

        /**
         * set response status.
         *
         * @param value response status.
         *
         */
        Response(String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return this.value;
        }
    }

    /**
     * error response map contains all the error status.
     */
    public static final Map<DeviceMessageErrorCode, Response> ERROR_RESPONSE = Map.of(
            DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED,
            Response.FAIL_MESSAGE_DELIVERY_TIMED_OUT,
            DeviceMessageErrorCode.DEVICE_STATUS_INACTIVE,
            Response.FAIL_VEHICLE_NOT_CONNECTED, DeviceMessageErrorCode.RETRY_ATTEMPTS_EXCEEDED,
            Response.FAIL_DELIVERY_RETRYING
    );
}
