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

package org.eclipse.ecsp.services.configurations;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * service config test case.
 */
public class ServicesConfigTestBase {
    
    public static final int RESPONSE_CODE_200 = 200;
    public static final int DELAY_11 = 11;
    public static final int SLEEP_IN_MILLIS = 100;

    protected Map<String, Integer> multiRequests(RestTemplate restTemplate, MockWebServer server, int loopCount,
                                               IgniteLogger logger) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(loopCount);
        Map<String, Integer> requestCountMap = new ConcurrentHashMap<>();
        
        for (int i = 0; i < loopCount; i++) {
            server.enqueue(new MockResponse()
                .setResponseCode(RESPONSE_CODE_200).setBody("ok").setHeadersDelay(DELAY_11, TimeUnit.SECONDS));
            
            final int requestId = i;
            new Thread(() -> {
                try {
                    logger.info("making request {}", requestId);
                    ResponseEntity<String> response = restTemplate.getForEntity(server.url("/ok").uri(), String.class);
                    
                    countDownLatch.countDown();
                    HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());
                    if (status != null) {
                        requestCountMap.compute(status.getReasonPhrase(), (k, v) -> v == null ? 0 : ++v);
                    }
                } catch (RestClientException e) {
                    String msg = ExceptionUtils.getRootCauseMessage(e);
                    logger.error(msg);
                    
                    countDownLatch.countDown();
                    requestCountMap.compute(msg.split(":")[0], (k, v) -> v == null ? 0 : ++v);
                }
            }, "request-" + requestId).start();
            if (!countDownLatch.await(SLEEP_IN_MILLIS, TimeUnit.MILLISECONDS)) {
                logger.warn("Timeout occurred while waiting for latch countdown");
            }
        }
        countDownLatch.await();
        logger.info("requestCountMap: {}", requestCountMap.toString());
        
        return requestCountMap;
    }
}
