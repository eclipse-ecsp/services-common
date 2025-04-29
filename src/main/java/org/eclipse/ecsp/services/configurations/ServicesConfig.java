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

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration for configuring {@link RestTemplate}.
 * with required read-timeout, connection-timeout,
 * connection-pool, total connection, connect per route.
 */
@Configuration
public class ServicesConfig {
    @Value("${rest.client.read.timeout:3000}")
    private int readTimeout;

    @Value("${rest.client.write.timeout:3000}")
    private int writeTimeout;

    @Value("${rest.client.connection.timeout:3000}")
    private int connectionTimeout;

    @Value("${rest.client.connection.request.timeout:3000}")
    private int connectionRequestTimeout;

    @Value("${rest.client.max.conn.total:20}")
    private int maxConnTotal;
    @Value("${rest.client.max.conn.per.route:2}")
    private int maxConnPerRoute;

    /**
     * Configure {@link RestTemplate} based on the provided
     * read-timeout, connection-timeout,
     * connection-pool, total connection, connect per route.
     *
     * @return instance of {@link RestTemplate}
     */
    @Bean("servicesCommonRestTemplate")
    RestTemplate restTemplate(@Qualifier("servicesCommonClientHttpRequestFactory")
                              ClientHttpRequestFactory httpRequestFactory) {
        return new RestTemplate(httpRequestFactory);
    }

    /**
     * Configure {@link ClientHttpRequestFactory} based on the provided
     * read-timeout, connection-timeout,
     * connection-pool, total connection, connect per route.
     *
     * @return instance of {@link ClientHttpRequestFactory}
     */
    @Bean("servicesCommonClientHttpRequestFactory")
    public ClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectionRequestTimeout(Timeout.ofMilliseconds(readTimeout))
                                .setResponseTimeout(Timeout.ofMilliseconds(connectionRequestTimeout))
                                .build())
                        .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                                .setMaxConnPerRoute(maxConnPerRoute)
                                .setMaxConnTotal(maxConnTotal)
                                .setDefaultSocketConfig(SocketConfig.custom()
                                        .setSoTimeout(Timeout.ofMilliseconds(readTimeout))
                                        .build())
                                .build())
                        .build());
        httpRequestFactory.setConnectTimeout(connectionTimeout);
        httpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);

        return httpRequestFactory;
    }
}