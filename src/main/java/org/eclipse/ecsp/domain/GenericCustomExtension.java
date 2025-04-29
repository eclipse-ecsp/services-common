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

package org.eclipse.ecsp.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import dev.morphia.annotations.Entity;
import java.util.Map;

/**
 * CustomExtension for ignite event.
 *
 * @author Neerajkumar
 */
@Entity
public class GenericCustomExtension {
    
    private Map<String, Object> customData;

    /**
     * initialize Generic Custom Extension.
     *
     * @param customData custom data.
     */
    public GenericCustomExtension(Map<String, Object> customData) {
        this.customData = customData;
    }

    /**
     * initialize Generic Custom Extension.
     */
    public GenericCustomExtension() {
    }

    /**
     * get custom data.
     *
     * @return custom data.
     */
    @JsonAnyGetter
    public Map<String, Object> getCustomData() {
        return customData;
    }

    /**
     * set custom data.
     *
     * @param customData custom data.
     */
    @JsonAnySetter
    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData;
    }
    
}
