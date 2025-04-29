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
import java.util.regex.Pattern;

/**
 * validate property value against regex.
 * test.prop3.property-validation={'required':'true','regex':'^S.+$'}
 *
 * @author eugene
 */
@SuppressWarnings("java:S6830")
@Component("regex-property-validator")
public class RegexPropertyValidator implements PropertyValidator {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(RegexPropertyValidator.class);
    
    @Override
    public String validate(String config, String name, String value) {
        String validationMessage = null;
        if (config == null || value == null) {
            LOGGER.debug(
                "property or regex value is null, ignore validate. property value: {}, regex: {}", value,
                config);
            return null;
        }
        
        Pattern p = Pattern.compile(config);
        
        if (!p.matcher(value).matches()) {
            validationMessage =
                String.format("property not match regex validation. property: %s, regex: %s", value,
                    config);
            LOGGER.debug(validationMessage);
        }
        
        return validationMessage;
    }
}
