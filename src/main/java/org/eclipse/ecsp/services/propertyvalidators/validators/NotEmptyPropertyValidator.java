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
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * property validation for not empty value.
 * test.prop2.property-validation={'not-empty':'true'}
 *
 * @author eugene
 */
@SuppressWarnings("java:S6830")
@Component("not-empty-property-validator")
public class NotEmptyPropertyValidator implements PropertyValidator {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(NotEmptyPropertyValidator.class);
    
    @Override
    public String validate(String config, String name, String value) {
        String validationMessage = null;
        
        if (Objects.isNull(value)) {
            return null;
        }
        if (StringUtils.equalsIgnoreCase("true", config) && StringUtils.isEmpty(value)) {
            validationMessage = String.format("property cannot empty, property name: %s", name);
            
            LOGGER.debug(validationMessage);
        }
        
        return validationMessage;
    }
}
