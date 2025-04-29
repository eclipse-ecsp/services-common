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

package org.eclipse.ecsp.services.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.services.exceptions.JsonParsingException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JsonValidator has utility methods to validate a json against a json schema.
 *
 * <b>property required: json.schemas</b>
 *
 * <p>json.schemas={schemaKey1:'path for the schema file1',schemaKey2:'path for the
 * schema file2'}
 *
 * <p>eg:
 *
 * <p>json.schemas={key1:file.json, key2:'classpath:/schema/file2.json',
 * key3:'file:/var/local/schema/file3.json',
 * key4:'http://example.com/schema/file3.json'}
 *
 * <p>Validate json against a schema
 */
@Component
public class JsonValidator {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(JsonValidator.class);
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings("java:S6857")
    @Value("#{${json.schemas:{:}}}")
    private Map<String, String> jsonSchemas;

    private final ResourceLoader resourceLoader;

    /**
     * Constructor to inject resource loader.
     *
     * @param resourceLoader resource loader
     */
    public JsonValidator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    private final Map<String, JsonSchema> schemaMap = new HashMap<>();
    
    /**
     * load json schema file and cache.
     */
    @PostConstruct
    public void loadSchemas() {
        LOGGER.debug("loading json schema:{}", jsonSchemas);
        JsonSchemaFactory factory =
            JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(VersionFlag.V7))
                .objectMapper(JsonValidator.OBJECT_MAPPER)
                .build();
        
        jsonSchemas.forEach((schemaKey, schemaPath) -> {
            if (schemaPath != null && !schemaPath.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)
                && !schemaPath.startsWith("file:")
                && !schemaPath.startsWith("http:")) {
                schemaPath = ResourceLoader.CLASSPATH_URL_PREFIX + schemaPath;
            }
            Resource resource = resourceLoader.getResource(schemaPath);
            if (resource.exists()) {
                try {
                    JsonSchema jsonschema = factory.getSchema(resource.getInputStream());
                    schemaMap.put(schemaKey, jsonschema);
                    LOGGER.info("Json schema {} from {} loaded successfully", schemaKey, schemaPath);
                } catch (Exception e) {
                    LOGGER.error("Error processing json schema:{}", schemaPath, e);
                    throw new IllegalStateException("Error Processing json schema: " + schemaPath, e);
                }
            } else {
                LOGGER.error("Json schema doesn't exists:{}", schemaPath);
                throw new IllegalStateException("json schema doesn't exists: " + schemaPath);
            }
        });
    }
    
    private List<String> validate(JsonNode content, JsonSchema jsonSchema) {
        Set<ValidationMessage> errors = jsonSchema.validate(content);
        if (errors.isEmpty()) {
            LOGGER.debug("Json schema validation completed, no validation error found");
            return Collections.emptyList();
        } else {
            LOGGER.error("Json schema {} validation error found", errors.size());
            return errors.stream().map(ValidationMessage::getMessage).toList();
        }
    }
    
    /**
     * Validate Object with the json schema.
     *
     * @param schemaName name of the schema
     * @param obj        Object to be validated againt the schema
     * @return list of validation errors
     * @throws RuntimeException if schema name is invalid , schema not found, error
     *                          converting object to json and if any exception encountered
     *                          while validating
     */
    public List<String> validate(String schemaName, Object obj) {
        if (obj == null) {
            LOGGER.error("Object for json validation must not be null");
            throw new IllegalArgumentException("Object for json validation must not be null");
        }
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(obj);
            return validate(schemaName, jsonString);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error processing json data:{}", obj, e);
            throw new JsonParsingException("Error while converting to json data: " + obj, e);
        }
        
    }
    
    /**
     * Validate json against the json schema.
     *
     * @param schemaName name of the schema
     * @param jsonString input JSON data as String
     * @return list of validation errors
     * @throws RuntimeException if schema name is invalid, schema not found, json is invalid
     *                          and if any exception encountered while validating
     */
    public List<String> validate(String schemaName, String jsonString) {
        LOGGER.debug("Json schema validation started, schemaName: {} and json: {}", schemaName,
            jsonString);
        if (!StringUtils.isEmpty(schemaName) && !StringUtils.isEmpty(jsonString)) {
            if (schemaMap.containsKey(schemaName)) {
                JsonSchema schema = schemaMap.get(schemaName);
                try {
                    JsonNode jsonData = OBJECT_MAPPER.readTree(jsonString);
                    return validate(jsonData, schema);
                } catch (Exception e) {
                    LOGGER.error("Error processing json data:{}", jsonString, e);
                    throw new JsonParsingException("Error while converting json data: " + jsonString, e);
                }
            } else {
                LOGGER.error("Json schema not found: {}", schemaName);
                throw new JsonParsingException("Schema not found:" + schemaName);
            }
        } else {
            LOGGER.error("JsonSchemaName:{} and jsonString:{} is invalid", schemaName, jsonString);
            throw new JsonParsingException("json/jsonSchemaName must not be null nor empty");
        }
    }
    
}
