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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class JsonValidatorFailedTest {
    JsonValidator validator;
    
    @BeforeEach
    void setUp() {
        // This method is called before each test
    }
    
    @Test
    void testFailed() {
        validator = new JsonValidator(new ClassRelativeResourceLoader(this.getClass()));
        ReflectionTestUtils.setField(validator, "jsonSchemas",
            Collections.singletonMap("app", "abcd.json"));
        assertThrows(RuntimeException.class, () -> {
            validator.loadSchemas();
        });
    }
    
    @Test
    void testFailedSchemaError() {
        validator = new JsonValidator(new ClassRelativeResourceLoader(this.getClass()));
        Resource resource = Mockito.mock(Resource.class);
        ResourceLoader loader = Mockito.mock(ResourceLoader.class);
        doReturn(false).when(resource).exists();
        when(loader.getResource(Mockito.anyString())).thenReturn(resource);
        
        ReflectionTestUtils.setField(validator, "jsonSchemas",
            Collections.singletonMap("app", "abcd.json"));
        ReflectionTestUtils.setField(validator, "resourceLoader", loader);
        assertThrows(RuntimeException.class, () -> {
            validator.loadSchemas();
        });
    }
    
    @Test
    @Disabled("after upgrade json validator version, unable reproduce")
    void testFailedInvalidSchema() throws IOException {
        validator = new JsonValidator(new ClassRelativeResourceLoader(this.getClass()));
        Resource resource = Mockito.mock(Resource.class);
        ReflectionTestUtils.setField(validator, "jsonSchemas",
            Collections.singletonMap("app", "invalid.json"));
        when(resource.exists()).thenReturn(true);
        InputStream schemaInputStream = this.getClass().getClassLoader()
            .getResourceAsStream("jsonValidation/test-schema-invalid.json");
        when(resource.getInputStream()).thenReturn(schemaInputStream);
        ResourceLoader loader = Mockito.mock(ResourceLoader.class);
        when(loader.getResource(Mockito.anyString())).thenReturn(resource);
        ReflectionTestUtils.setField(validator, "resourceLoader", loader);
        assertThrows(RuntimeException.class, () -> {
            validator.loadSchemas();
        });
    }
    
    @Test
    void testFailedInvalidJson() throws IOException {
        validator = new JsonValidator(new ClassRelativeResourceLoader(this.getClass()));
        ReflectionTestUtils.setField(validator, "schemaMap", Collections.singletonMap("test", null));
        ObjectMapper mapper = Mockito.mock(ObjectMapper.class);
        when(mapper.writeValueAsString(Mockito.any())).thenThrow(JsonProcessingException.class);
        assertThrows(RuntimeException.class, () -> {
            validator.validate("test", validator);
        });
    }
}
