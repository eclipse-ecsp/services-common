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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.services.ServiceCommonTestConfig;
import org.eclipse.ecsp.services.ServicesTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ServiceCommonTestConfig.class)
class JsonValidatorTest extends ServicesTestBase {
    @Autowired
    private JsonValidator validator;
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Test
    void testValidateSuccess() throws IOException {
        String json = IOUtils.toString(
                new ClassPathResource("jsonValidation/test.json").getInputStream(),
                StandardCharsets.UTF_8);
        List<String> errors = validator.validate("test", json);
        assertEquals(0, errors.size());
    }
    
    @Test
    void testValidateNull() {
        assertThrows(RuntimeException.class, () -> {
            validator.validate(null, null);
        });
    }
    
    @Test
    void testValidateNullObject() {
        assertThrows(RuntimeException.class, () -> {
            validator.validate("test", null);
        });
    }
    
    @Test
    void testValidateEmptySchemaName() {
        assertThrows(RuntimeException.class, () -> {
            validator.validate("", null);
        });
    }
    
    @Test
    void testValidateInvalidJson() throws IOException {
        String json = IOUtils.toString(
                new ClassPathResource("jsonValidation/test-invalid.json").getInputStream(),
                StandardCharsets.UTF_8);
        List<String> errors = validator.validate("test", json);
        assertEquals(1, errors.size());
    }
    
    @Test
    void testValidateInvalidJsonData() throws IOException {
        String json = IOUtils.toString(
                new ClassPathResource("jsonValidation/test.json").getInputStream(),
                StandardCharsets.UTF_8);
        assertThrows(RuntimeException.class, () -> {
            validator.validate("test", "askasdakjndkj" + json + "adbajb");
        });
    }
    
    @Test
    void testValidateWithObject() {
        List<String> errors =
            validator.validate("test", new Address("Adriaan Goekooplaan", "Netherlands", "2517 JX"));
        assertEquals(0, errors.size());
    }
    
    @Test
    void testValidateWithObjectNullSchemaName() {
        Address address = new Address("Adriaan Goekooplaan", "Netherlands", "2517 JX");
        assertThrows(RuntimeException.class, () -> {
            validator.validate(null, address);
        });
    }
    
    @Test
    void testValidateWithObjectEmptySchemaName() {
        Address address = new Address("Adriaan Goekooplaan", "Netherlands", "2517 JX");
        assertThrows(RuntimeException.class, () -> {
            validator.validate("", address);
        });
    }
    
    @Test
    void testValidateWithObjectInvalid() {
        List<String> errors =
            validator.validate("test", new Address("Adriaan Goekooplaan", "Netherlands", "sjfjad"));
        assertEquals(1, errors.size());
    }
    
    @Test
    void testValidateFailedInvalidKey() {
        Address address = new Address("Adriaan Goekooplaan", "Netherlands", "sjfjad");
        
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            validator.validate("test12", address);
        });
        
        assertTrue(e.getMessage().contains("Schema not found"));
    }
    
    @Test
    void testValidateInvalidSchemaName() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            validator.validate("test12", "{}");
        });
        
        assertTrue(e.getMessage().contains("Schema not found"));
    }
    
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {
    }
    
    public static class Address {
        @JsonProperty("street_address")
        private String streetAddress;
        private String country;
        @JsonProperty("postal_code")
        private String postalCode;
        
        public Address() {
        
        }
        
        public Address(String streetAddress, String country, String postalCode) {
            this.streetAddress = streetAddress;
            this.country = country;
            this.postalCode = postalCode;
        }
        
        public String getStreetAddress() {
            return streetAddress;
        }
        
        public void setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getPostalCode() {
            return postalCode;
        }
        
        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
        
    }
}
