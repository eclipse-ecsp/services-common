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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class MaxPropertyValidatorTest {
    
    private final PropertyValidator propertyValidator = new MaxPropertyValidator();
    
    @Test
    void validate() {
        assertNotNull(propertyValidator.validate("2", "prop-name", "3"));
    }
    
    @Test
    void validate_pass() {
        assertNull(propertyValidator.validate("4", "prop-name", "3"));
    }
    
    @Test
    void validate_pass_eq_max() {
        assertNull(propertyValidator.validate("4", "prop-name", "4"));
    }
    
    @Test
    void validate_ignore_config() {
        assertNull(propertyValidator.validate("", "prop-name", "3"));
    }
    
    @Test
    void validate_ignore_value() {
        assertNull(propertyValidator.validate("4", "prop-name", ""));
    }
    
    @Test
    void validate_ignore_config_not_number() {
        assertNotNull(propertyValidator.validate("abcd", "prop-name", "3"));
    }
    
    @Test
    void validate_ignore_value_not_number() {
        assertNotNull(propertyValidator.validate("4", "prop-name", "abcd"));
    }
}