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

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.services.exceptions.TransformerException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility Class for transforming an Ignite event to custom JSON string. This
 * class also provides a method to transform JSON string to JSON string too.
 *
 * <p>The Class uses Jolt tool to do the transform and needs a jolt specification
 * as a resource file path. The jolt specification file is a JSON file that
 * describes the transformation. Please refer to
 * https://github.com/bazaarvoice/jolt for more information on how to construct
 * a specification file for a desired output JSON
 */
public class IgniteEventToJsonTransformer {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(IgniteEventToJsonTransformer.class);
    private final Chainr chainr;

    /**
     * Constructor for {@link IgniteEventToJsonTransformer}.
     *
     * @param specFileResourcePath the path to the json specification file
     */
    public IgniteEventToJsonTransformer(final Path specFileResourcePath) {
        Objects.requireNonNull(specFileResourcePath, "spec file path cannot be null");
        chainr = loadSpec(specFileResourcePath.toString());
    }

    /**
     * Constructor for {@link IgniteEventToJsonTransformer}.
     *
     * @param jsonSpec the JSON specification as a string
     */
    public IgniteEventToJsonTransformer(final String jsonSpec) {
        Objects.requireNonNull(jsonSpec);
        chainr = loadSpecFromJson(jsonSpec);
    }
    
    /**
     * Convert provided json to provided json spec.
     *
     * @param inputJson                provided json
     * @param specJsonResourceFilePath json spec path
     * @return transformed json to provided spec
     */
    public static String transform(final String inputJson, final String specJsonResourceFilePath) {
        Object transformedOutput = loadSpec(specJsonResourceFilePath)
            .transform(JsonUtils.jsonToObject(inputJson));
        return JsonUtils.toJsonString(transformedOutput);
    }
    
    
    /**
     * converting {@link IgniteEvent} to json string.
     *
     * @param event serialize json from
     * @return serialized json string
     */
    public String transform(final IgniteEvent event) {
        assert (chainr != null);
        String vehicleProfileNotification = "";
        try {
            vehicleProfileNotification = ServiceUtil.createJsonMapperForIgniteEvent()
                .writeValueAsString(event);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to convert the ignite event to json: {}", e.getMessage());
            throw new TransformerException(e);
        }
        Object transformedOutput = chainr.transform(JsonUtils.jsonToObject(vehicleProfileNotification));
        return JsonUtils.toJsonString(transformedOutput);
    }
    
    private static Chainr loadSpec(final String specFileResourcePath) {
        if (ServiceUtil.doesResourceFileExist(specFileResourcePath)) {
            return Chainr.fromSpec(JsonUtils.classpathToList(specFileResourcePath));
        }
        throw new TransformerException(
            "Cannot find the specification resource file for json transformer: "
                + specFileResourcePath);
    }
    
    private static Chainr loadSpecFromJson(final String specJson) {
        if (StringUtils.isEmpty(specJson)) {
            throw new IllegalArgumentException("Invalid spec content, spec cannot be empty");
        }
        return Chainr.fromSpec(JsonUtils.jsonToList(specJson));
    }
}
