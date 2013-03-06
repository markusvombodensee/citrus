/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.admin.service;

import org.springframework.stereotype.Component;

/**
 * Single point of access for all configuration settings
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.26
 */
@Component
public class ConfigService {
    /** System property name for project home setting */
    public static final String PROJECT_HOME = "project.home";
    
    public static final String ROOT_DIRECTORY = "root.directory";

    /**
     * Get project home from system property.
     * @return
     */
    public String getProjectHome() {
        return System.getProperty(PROJECT_HOME);
    }
    
    /**
     * Gets the root directory from system property. By default user.home system
     * property setting is used as root.
     * @return
     */
    public String getRootDirectory() {
        return System.getProperty(ROOT_DIRECTORY, System.getProperty("user.home"));
    }

    /**
     * Sets new project home path.
     * @param homePath
     */
    public void setProjectHome(String homePath) {
        System.setProperty(PROJECT_HOME, homePath);
    }
    
}