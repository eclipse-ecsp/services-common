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

package org.eclipse.ecsp.services.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Map;

/**
 * MongoDB server for testing.
 *
 * <p>This class is responsible for starting and stopping an embedded MongoDB server
 * using Testcontainers. It also provides methods to get the host and port of the
 * running MongoDB instance.
 */
@Testcontainers
public class MongoServer extends ExternalResource {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(MongoServer.class);

    @Container
    private static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer("mongo:6.0.22");
    private static int port = 0;

    @Override
    public void before() {
        if (!MONGO_CONTAINER.isRunning()) {
            MONGO_CONTAINER.start();
            port = MONGO_CONTAINER.getFirstMappedPort();
        }
        System.setProperty("mongodb.hosts", MONGO_CONTAINER.getReplicaSetUrl());
        System.setProperty("mongodb.port", String.valueOf(MONGO_CONTAINER.getFirstMappedPort()));
        LOGGER.info("Embedded mongo DB started on port {} ", port);
        LOGGER.info("MongoClient connecting for pre-work DB configuration...");
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:" + port)) {
            Map<String, Object> commandArguments = new BasicDBObject();
            commandArguments.put("createUser", "admin");
            commandArguments.put("pwd", "password");
            String[] roles = {"readWrite"};
            commandArguments.put("roles", roles);
            BasicDBObject command = new BasicDBObject(commandArguments);
            MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
            adminDatabase.runCommand(command);
        } catch (Exception e) {
            LOGGER.error("Error while creating user in mongo DB", e);
        }
    }

    @Override
    public void after() {
        if (MONGO_CONTAINER.isRunning()) {
            MONGO_CONTAINER.stop();
        }
    }

    /**
     * stop embedded mongoDB server.
     */
    public void kill() {
        after();
        MONGO_CONTAINER.close();
    }

    /**
     * Returns the mongoDB host.
     *
     * @return the mongoDB host
     */
    public String getMongoServerHost() {
        return MONGO_CONTAINER.getHost();
    }

    /**
     * Returns the mongoDB port.
     *
     * @return the mongoDB port
     */
    public int getMongoServerPort() {
        return MONGO_CONTAINER.getFirstMappedPort();
    }
}
