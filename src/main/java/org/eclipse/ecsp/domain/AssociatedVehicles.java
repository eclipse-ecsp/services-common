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

import java.util.List;

/**
 * pojo for list of associated vehicle.
 *
 * @author Neerajkumar
 */
public class AssociatedVehicles {
    
    private String message;
    
    private List<AssociatedVehicle> data;

    /**
     * get associated message.
     *
     * @return message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * set associated message.
     *
     * @param message message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * get associated vehicle information.
     *
     * @return list of associated vehicles.
     */
    public List<AssociatedVehicle> getData() {
        return data;
    }

    /**
     * set associated vehicle information.
     *
     * @param data  list of associated vehicles.
     */
    public void setData(List<AssociatedVehicle> data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "AssociatedVehicles [message=" + message + ", data=" + data + "]";
    }
    
}
