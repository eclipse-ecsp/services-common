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

package org.eclipse.ecsp.services.propertyvalidators.validators;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Property validation for value data type.
 * test.prop5.property-validation={'type':'double'}
 */
@SuppressWarnings("java:S6830")
@Component("type-property-validator")
public class TypePropertyValidator implements PropertyValidator {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(TypePropertyValidator.class);
    private static final String VALIDATION_MESSAGE =
        "covert value type fail. property name: %s, value: %s, type: %s";
    
    @Override
    public String validate(String config, String name, String value) {
        String validationMessage = null;
        if (StringUtils.isEmpty(config) || StringUtils.isEmpty(value)) {
            LOGGER.debug(
                "config or value is empty, ignore validate. property name: {}, value: {}, config: {}",
                name, value, config);
            return null;
        }
        
        switch (config.toUpperCase()) {
            case "INTEGER":
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    LOGGER.debug(ExceptionUtils.getRootCauseMessage(e));
                    validationMessage = buildValidationMessage(name, value, config);
                }
                break;
            case "LONG":
                try {
                    Long.valueOf(value);
                } catch (NumberFormatException e) {
                    LOGGER.debug(ExceptionUtils.getRootCauseMessage(e));
                    validationMessage = buildValidationMessage(name, value, config);
                }
                break;
            case "DOUBLE":
                try {
                    Double.valueOf(value);
                } catch (NumberFormatException e) {
                    LOGGER.debug(ExceptionUtils.getRootCauseMessage(e));
                    validationMessage = buildValidationMessage(name, value, config);
                }
                break;
            case "FLOAT":
                try {
                    Float.valueOf(value);
                } catch (NumberFormatException e) {
                    LOGGER.debug(ExceptionUtils.getRootCauseMessage(e));
                    validationMessage = buildValidationMessage(name, value, config);
                }
                break;
            case "BOOLEAN":
                if (!(StringUtils.equalsIgnoreCase(value, "true")
                    || StringUtils.equalsIgnoreCase(value, "false"))) {
                    validationMessage = buildValidationMessage(name, value, config);
                }
                break;
            default:
                validationMessage = String.format("unsupported type, type: %s", config);
        }
        
        return validationMessage;
    }
    
    private String buildValidationMessage(String name, String value, String config) {
        return String.format(VALIDATION_MESSAGE, name, value, config);
    }
}
