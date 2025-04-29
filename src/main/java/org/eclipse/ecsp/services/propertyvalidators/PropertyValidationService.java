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

package org.eclipse.ecsp.services.propertyvalidators;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.services.exceptions.PropertiesValidationException;
import org.eclipse.ecsp.services.propertyvalidators.validators.PropertyValidator;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * Validate application properties and its value.
 * if validation is enable property-validation-enable: true
 *
 * @author eugene
 */
@Component
public class PropertyValidationService {
    /**
     * .property-validation.
     */
    public static final String PROPERTY_VALIDATION_SUFFIX = ".property-validation";
    /**
     * -property-validator.
     */
    public static final String VALIDATOR_NAME_SUFFIX = "-property-validator";
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(PropertyValidationService.class);
    static ObjectMapper objectMapper = new ObjectMapper();
    private final Environment env;
    private final ApplicationContext applicationContext;
    @Value("${property-validation-enable:true}")
    private boolean propertyValidationEnable;

    /**
     * Constructor to inject resource loader.
     *
     * @param env environment
     */
    public PropertyValidationService(ApplicationContext context, Environment env) {
        this.env = env;
        this.applicationContext = context;
    }

    /**
     * get the status of validation is enabled or disabled.
     * property-validation-enable: true
     *
     * @return true if validation is enabled, otherwise false.
     */
    public boolean getPropertyValidationEnable() {
        return propertyValidationEnable;
    }
    
    /**
     * trigger application property and its value validation.
     */
    @PostConstruct
    public void init() {
        if (propertyValidationEnable) {
            validateProperties();
        } else {
            LOGGER.info("property validation not enable");
        }
    }
    
    /**
     * Validate application properties.
     *
     * @author eugene
     */
    public void validateProperties() {
        LOGGER.info("properties validation start");
        // load all properties from env
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        List<String> messageList = StreamSupport.stream(propSrcs.spliterator(), false)
            .filter(EnumerablePropertySource.class::isInstance)
            .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
            .flatMap(Arrays::stream)
            .filter(propName -> propName.endsWith(PROPERTY_VALIDATION_SUFFIX))
            .distinct()
            .map(this::validateByConfig)
            .flatMap(Collection::stream)
            .toList();
        
        if (!messageList.isEmpty()) {
            LOGGER.error("properties validation failed, number of condition failed: {}",
                messageList.size());
            throw new PropertiesValidationException(messageList);
        }
        LOGGER.info("successfully validate properties.");
    }
    
    private List<String> validateByConfig(String validateConfigPropertyName) {
        String validationConfig = env.getProperty(validateConfigPropertyName);
        
        if (Objects.isNull(validationConfig)) {
            return Collections.emptyList();
        }
        
        Map<String, String> validationConfigMap = getValidationConfigMap(validationConfig);
        
        // property to validate
        String propertyNameToValidate = validateConfigPropertyName.substring(0,
            validateConfigPropertyName.length() - PROPERTY_VALIDATION_SUFFIX.length());
        String propertyValueToValidate = env.getProperty(propertyNameToValidate);
        LOGGER.debug("property to validate, name: {}, value: {}, condition: {}",
            propertyNameToValidate,
            propertyValueToValidate,
            validationConfig);
        
        List<String> msgList = new ArrayList<>();
        for (Map.Entry<String, String> validationCon : validationConfigMap.entrySet()) {
            String validatorConfig = validationCon.getValue();
            String msg = validateProperty(validationCon.getKey(), validatorConfig, propertyNameToValidate,
                propertyValueToValidate);
            
            if (StringUtils.isNotEmpty(msg)) {
                msgList.add(msg);
            }
        }
        return msgList;
    }
    
    private String validateProperty(String validatorName, String validatorConfig, String propertyName,
                                    String propertyValue) {
        String validatorBeanName = validatorName + VALIDATOR_NAME_SUFFIX;
        
        String msg;
        
        try {
            PropertyValidator propertyValidator =
                applicationContext.getBean(validatorBeanName, PropertyValidator.class);
            
            msg = propertyValidator.validate(validatorConfig, propertyName, propertyValue);
        } catch (BeansException e) {
            msg = String.format("property validator not found, name: %s", validatorBeanName);
            LOGGER.error(msg, e);
        }
        return msg;
    }
    
    private Map<String, String> getValidationConfigMap(String config) {
        try {
            return objectMapper.readValue(config.replace('\'', '"'), Map.class);
        } catch (IOException e) {
            LOGGER.error("validation config convert to map fail", e);
        }
        return Collections.emptyMap();
    }
}
