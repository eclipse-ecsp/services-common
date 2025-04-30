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

package org.eclipse.ecsp.services;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.services.mongo.MongoServer;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all service tests.
 */
@Testcontainers
public class ServicesTestBase {

    protected ServicesTestBase() {
        // Prevent instantiation
    }

    @ClassRule
    public static final MongoServer MONGO_DB = new MongoServer();

    @BeforeAll
    static void setUp() {
        MONGO_DB.before();
        CollectorRegistry.defaultRegistry.clear();
    }

    @AfterAll
    static void tearDown() {
        MONGO_DB.after();
        CollectorRegistry.defaultRegistry.clear();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.hosts", MONGO_DB::getMongoServerHost);
        registry.add("mongodb.port", MONGO_DB::getMongoServerPort);
    }
}
