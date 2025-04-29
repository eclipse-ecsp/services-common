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
import java.math.BigDecimal;

/**
 * property validation for min value.
 * test.prop5.property-validation={'type':'double', 'min':'100.1'}
 *
 * @author eugene
 */
@SuppressWarnings("java:S6830")
@Component("min-property-validator")
public class MinPropertyValidator implements PropertyValidator {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(MinPropertyValidator.class);
    
    @Override
    public String validate(String config, String name, String value) {
        String validationMessage = null;
        if (StringUtils.isEmpty(config) || StringUtils.isEmpty(value)) {
            LOGGER.debug(
                "config or value is null, ignore validate. property name: {}, value: {}, config: {}",
                name, value, config);
            return null;
        }
        
        BigDecimal min;
        BigDecimal val;
        
        try {
            min = new BigDecimal(config);
            val = new BigDecimal(value);
            
            if (val.compareTo(min) < 0) {
                validationMessage =
                    String.format("value is less than min. property name: %s, value: %s, min: %s", name,
                        value, config);
            }
        } catch (NumberFormatException e) {
            validationMessage = String.format(
                "covert config or value to number fail. property name: %s, value: %s, config: %s", name,
                value, config);
        }
        
        return validationMessage;
    }
}
