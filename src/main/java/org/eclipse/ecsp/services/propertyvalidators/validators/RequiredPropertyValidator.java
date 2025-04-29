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

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Component;

/**
 * validate if property required.
 * test.prop1.property-validation={'required':'true'}
 *
 * @author eugene
 */
@SuppressWarnings("java:S6830")
@Component("required-property-validator")
public class RequiredPropertyValidator implements PropertyValidator {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(RequiredPropertyValidator.class);
    
    @Override
    public String validate(String config, String name, String value) {
        String validationMessage = null;
        
        if (Boolean.TRUE.equals(Boolean.valueOf(config)) && value == null) {
            validationMessage = String.format("property is required, property name: %s", name);
            
            LOGGER.debug(validationMessage);
        }
        
        return validationMessage;
    }
}
