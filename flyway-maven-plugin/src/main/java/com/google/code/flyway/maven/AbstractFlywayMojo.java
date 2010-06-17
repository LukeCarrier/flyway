/**
 * Copyright (C) 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.flyway.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Driver;

/**
 * Description.<br>
 */
abstract class AbstractFlywayMojo extends AbstractMojo {

    /**
     * The jdbc driver to use to connect to the database.
     *
     * @parameter
     * @required
     */
    protected String driver = null;

    /**
     * The url to use to connect to the database.
     *
     * @parameter
     * @required
     */
    protected String url;

    /**
     * The user to use to connect to the database.
     *
     * @parameter
     * @required
     */
    protected String user;

    /**
     * The password to use to connect to the database.
     *
     * @parameter
     */
    protected String password = "";

    protected DataSource getDataSource() throws MojoExecutionException {
        try {
            Driver driverClazz = (Driver) Class.forName(driver).newInstance();
            return new SimpleDriverDataSource(driverClazz, url, user, password);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Unable to find driver class: " + driver, e);
        } catch (InstantiationException e) {
            throw new MojoExecutionException("Unable to instantiate driver class: " + driver, e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("Unable to access driver class: " + driver, e);
        }
    }

}
