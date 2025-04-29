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

import org.eclipse.ecsp.domain.DmaResponse.Response;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Common Event Constants.
 */
public class Constants {

    private Constants() {}

    /**
     * VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT.
     */
    public static final String EVENT_VEHICLE_CHANGE_NOTIFICATION =
        "VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT";

    /**
     * VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT.
     */
    public static final String EVENT_VEHICLE_CREATION_NOTIFICATION =
        "VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT";

    /**
     * application/json.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * content-type.
     */
    public static final String CONTENT_TYPE = "content-type";
    
    // Response Enum
    /**
     * SUCCESS.
     */
    public static final String SUCCESS = "SUCCESS";
    /**
     * FAIL.
     */
    public static final String FAIL = "FAIL";
    /**
     * FAIL_DELIVERY_RETRYING.
     */
    public static final String FAIL_DELIVERY_RETRYING = "FAIL_DELIVERY_RETRYING";
    /**
     * FAIL_VEHICLE_NOT_CONNECTED.
     */
    public static final String FAIL_VEHICLE_NOT_CONNECTED = "FAIL_VEHICLE_NOT_CONNECTED";
    /**
     * CUSTOM_EXTENSION.
     */
    public static final String CUSTOM_EXTENSION = "CUSTOM_EXTENSION";
    
    // Remote Inhibit
    /**
     * RIRequest.
     */
    public static final String EVENT_ID_REMOTE_INHIBIT_REQUEST = "RIRequest";
    /**
     * RIResponse.
     */
    public static final String EVENT_ID_REMOTE_INHIBIT_RESPONSE = "RIResponse";
    /**
     * CrankNotificationData.
     */
    public static final String EVENT_ID_CRANK_NOTIFICATION_DATA = "CrankNotificationData";
    /**
     * remoteInhibit.
     */
    public static final String REMOTE_INHIBIT = "remoteInhibit";
    /**
     * FAIL_MESSAGE_DELIVERY_TIMED_OUT.
     */
    public static final String FAIL_MESSAGE_DELIVERY_TIMED_OUT = "FAIL_MESSAGE_DELIVERY_TIMED_OUT";
    /**
     * RETRYING_DEVICE_DELIVERY_MESSAGE.
     */
    public static final String RETRYING_DEVICE_DELIVERY_MESSAGE = "RETRYING_DEVICE_DELIVERY_MESSAGE";
    /**
     * RETRYING_AUTOMATICALLY_FOR_7_DAYS.
     */
    public static final String RETRYING_AUTOMATICALLY_FOR_7_DAYS =
        "RETRYING_AUTOMATICALLY_FOR_7_DAYS";
    /**
     * FAILURE.
     */
    public static final String FAILURE = "FAILURE";

    /**
     * userKey.
     */
    public static final String USER_KEY = "userKey";
    /**
     * secret.
     */
    public static final String SECRET_KEY = "secret";
    /**
     * apiKey.
     */
    public static final String API_KEY = "apiKey";
    /**
     * uid.
     */
    public static final String UID_KEY = "uid";
    /**
     * sub.
     */
    public static final String SUB = "sub";
    /**
     * DEFAULT.
     */
    public static final String DEFAULT_PROVIDER = "DEFAULT";

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
    
    // VehicleProfileChangedNotificationEventUtil query path
    /**
     * MAP_KEY_USER_ID_PATH.
     */
    public static final String MAP_KEY_USER_ID_PATH = "MAP_KEY_USER_ID_PATH";
    /**
     * MAP_KEY_VEHICLE_ID_PATH.
     */
    public static final String MAP_KEY_VEHICLE_ID_PATH = "MAP_KEY_VEHICLE_ID_PATH";
    /**
     * UNKNOWN.
     */
    public static final String USER_ID_UNKNOWN = "UNKNOWN";

    /**
     * yyyy-MM-dd'T'HH:mm:ss.SSSxx.
     */
    public static final DateTimeFormatter DTF_VEHICLE_PROFILE =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxx");
}
