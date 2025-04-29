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

package org.eclipse.ecsp.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * converting {@link AssociationFailedException} <br/>
 * to response with status code 500 - Internal Server Error.
 *
 * @author Neerajkumar
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class AssociationFailedException extends Exception {
    
    private static final long serialVersionUID = -6856642719277283639L;
    
    public AssociationFailedException(String message) {
        super(message);
    }
    
    public AssociationFailedException(String message, String cause) {
        super(message, new Throwable(cause));
    }
}
