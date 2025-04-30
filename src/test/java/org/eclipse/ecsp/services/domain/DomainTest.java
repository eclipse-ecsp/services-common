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

package org.eclipse.ecsp.services.domain;

import org.eclipse.ecsp.domain.Application;
import org.eclipse.ecsp.domain.DmaResponse;
import org.eclipse.ecsp.domain.DmaResponse.Response;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Map;

/**
 * Test class for {@link DmaResponse} and {@link Application}.
 */
class DomainTest {

    @Test
    void testApplicationDomains() {
        Application application = new Application();
        application.setApplicationId("TestApp");
        application.setVersion("1.0");
        Assertions.assertEquals("TestApp", application.getApplicationId());
        Assertions.assertEquals("1.0", application.getVersion());
    }

    @Test
    void testDmaResponse() {
        Assertions.assertEquals("FAIL_DELIVERY_RETRYING", Response.FAIL_DELIVERY_RETRYING.name());
        Assertions.assertEquals("FAIL_VEHICLE_NOT_CONNECTED", Response.FAIL_VEHICLE_NOT_CONNECTED.name());
        Assertions.assertEquals("SUCCESS", Response.SUCCESS.name());
        Assertions.assertEquals("FAIL", Response.FAIL.name());
        Assertions.assertEquals("FAIL_MESSAGE_DELIVERY_TIMED_OUT",
                Response.FAIL_MESSAGE_DELIVERY_TIMED_OUT.name());
        Assertions.assertEquals("RETRYING_AUTOMATICALLY_FOR_7_DAYS",
                Response.RETRYING_AUTOMATICALLY_FOR_7_DAYS.name());
        Assertions.assertEquals("RETRYING_DEVICE_DELIVERY_MESSAGE",
                Response.RETRYING_DEVICE_DELIVERY_MESSAGE.name());

        Map<DeviceMessageErrorCode, Response> errorResponses = DmaResponse.ERROR_RESPONSE;
        Assertions.assertEquals(Response.FAIL_VEHICLE_NOT_CONNECTED,
                errorResponses.get(DeviceMessageErrorCode.DEVICE_STATUS_INACTIVE));
        Assertions.assertEquals(Response.FAIL_MESSAGE_DELIVERY_TIMED_OUT,
                errorResponses.get(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED));
        Assertions.assertEquals(Response.FAIL_DELIVERY_RETRYING,
                errorResponses.get(DeviceMessageErrorCode.RETRY_ATTEMPTS_EXCEEDED));
    }
}
