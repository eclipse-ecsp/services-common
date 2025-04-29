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


class TypePropertyValidatorTest {
    private final PropertyValidator propertyValidator = new TypePropertyValidator();
    
    @Test
    void validate_ignoreValidate() {
        assertNull(propertyValidator.validate(null, "prop-name", ""));
    }
    
    @Test
    void validate_int_pass() {
        assertNull(propertyValidator.validate("integer", "prop-name", "111"));
    }
    
    @Test
    void validate_int_fail() {
        assertNotNull(propertyValidator.validate("integer", "prop-name", "Abc"));
    }
    
    @Test
    void validate_int_fail_gt_max() {
        assertNotNull(propertyValidator.validate("integer", "prop-name", "2147483648"));
    }
    
    @Test
    void validate_long_pass() {
        assertNull(propertyValidator.validate("long", "prop-name", "9223372036854775807"));
    }
    
    @Test
    void validate_long_fail_gt_max() {
        assertNotNull(propertyValidator.validate("long", "prop-name", "9223372036854775809"));
    }
    
    @Test
    void validate_double() {
        assertNull(propertyValidator.validate("double", "prop-name", "111.1"));
    }
    
    @Test
    void validate_double_exception() {
        assertNotNull(propertyValidator.validate("double", "prop-name", "ABCD"));
    }
    
    @Test
    void validate_float() {
        assertNull(propertyValidator.validate("float", "prop-name", "111.2"));
    }
    
    @Test
    void validate_float_exception() {
        assertNotNull(propertyValidator.validate("float", "prop-name", "ABCD"));
    }
    
    @Test
    void validate_not_support() {
        assertNotNull(propertyValidator.validate("number", "prop-name", "111"));
    }
    
    @Test
    void validate_boolean_success() {
        assertNull(propertyValidator.validate("boolean", "prop-name", "true"));
    }
    
    @Test
    void validate_boolean_false() {
        assertNull(propertyValidator.validate("BOOLEAN", "prop-name", "FALSE"));
    }
    
    @Test
    void validate_boolean_null() {
        assertNull(propertyValidator.validate("boolean", "prop-name", null));
    }
    
    @Test
    void validate_invalid_boolean() {
        assertNotNull(propertyValidator.validate("boolean", "prop-name", "ok"));
    }
}