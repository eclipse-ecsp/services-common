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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * {@link ApplicationContextAware} implementation to store {@link ApplicationContext}.
 *
 * @author Neerajkumar
 */
@SuppressWarnings({"java:S2696", "java:S6548"})
@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContextUtil applicationContextUtil;
    private ApplicationContext appContext;
    
    /**
     * fetch the stored application context.
     *
     * @return ApplicationContext
     */
    public static synchronized ApplicationContextUtil getInstance() {
        if (null == applicationContextUtil) {
            applicationContextUtil = new ApplicationContextUtil();
        }
        return applicationContextUtil;
    }

    /**
     * fetch the application context.
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return ApplicationContextUtil.getInstance().getAppContext();
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtil.getInstance().setAppContext(applicationContext);
    }

    /**
     * get the application context.
     *
     * @return ApplicationContext
     */
    public ApplicationContext getAppContext() {
        return appContext;
    }

    /**
     * set the application context.
     *
     * @param appContext ApplicationContext
     */
    public void setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }
}
