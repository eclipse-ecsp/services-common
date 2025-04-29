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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ServicesConfig.class)
@TestPropertySource("classpath:/rest-template-test.properties")
class ServicesConfigTest extends ServicesConfigTestBase {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ServicesConfigTest.class);
    public static final int RESPONSE_CODE_200 = 200;
    public static final int RESPONSE_CODE_404 = 404;
    public static final int DELAY_11 = 11;
    public static final int LOOP_COUNT = 10;
    public static final int FIVE = 5;
    @Rule
    public final MockWebServer server = new MockWebServer();
    @Autowired
    RestTemplate restTemplate;
    
    @Test
    void restTemplateClientFactoryType() {
        assertTrue(restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory);
    }
    
    @Test
    void responseOk() {
        server.enqueue(
            new MockResponse().setResponseCode(RESPONSE_CODE_200).setBody("ok")
        );
        
        ResponseEntity<String> response = restTemplate.getForEntity(server.url("/ok").uri(), String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void responseNotFound() {
        server.enqueue(
            new MockResponse().setResponseCode(RESPONSE_CODE_404)
        );
        
        assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(server.url("/ok").uri(), String.class);
        });
    }
    
    @Test
    void responseDelay() {
        server.enqueue(
            new MockResponse().setResponseCode(RESPONSE_CODE_200).setBody("ok")
                .setHeadersDelay(DELAY_11, TimeUnit.SECONDS)
        );
        URI uri = server.url("/ok").uri();
        try {
            restTemplate.getForEntity(uri, String.class);
            Assertions.fail("Expected ResourceAccessException");
        } catch (ResourceAccessException e) {
            Assertions.assertTrue( () -> StringUtils.contains(e.getMessage(), "Read timed out"));
        }
    }

    @Test
    void connectionPoolTimeoutException() throws InterruptedException {
        Map<String, Integer> requestCountMap = multiRequests(restTemplate, server, LOOP_COUNT, LOGGER);
        assertTrue(requestCountMap.getOrDefault("ConnectionRequestTimeoutException", 0) > FIVE);
    }
}