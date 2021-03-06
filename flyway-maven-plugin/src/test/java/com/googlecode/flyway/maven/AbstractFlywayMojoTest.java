/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.exception.FlywayException;
import org.junit.Test;

import javax.sql.DataSource;

import static org.junit.Assert.assertFalse;

/**
 * Test for the AbstractFlywayMojo.
 */
@SuppressWarnings({"JavaDoc"})
public class AbstractFlywayMojoTest {
    /**
     * Tests that the datasource is properly closed and not leaking connections.
     */
    @Test
    public void dataSourceClosed() throws Exception {
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
                flyway.init();
            }
        };
        assertNoConnectionLeak(mojo);
    }

    /**
     * Tests that the datasource is properly closed and not leaking connections, even when an exception was thrown.
     */
    @Test
    public void dataSourceClosedAfterException() throws Exception {
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
                flyway.init();
                throw new FlywayException("Wanted failure");
            }
        };
        assertNoConnectionLeak(mojo);
    }

    /**
     * Asserts that this mojo does not leak DB connections when executed.
     *
     * @param mojo The mojo to check.
     */
    private void assertNoConnectionLeak(AbstractFlywayMojo mojo) throws Exception {
        mojo.driver = "org.h2.Driver";
        mojo.url = "jdbc:h2:mem:flyway_leak_test";
        mojo.user = "SA";

        try {
            mojo.execute();
        } catch (Exception e) {
            // Ignoring. The exception is not what we're testing.
        }

        DataSource dataSource = mojo.createDataSource();
        DbSupport dbSupport = DbSupportFactory.createDbSupport(dataSource.getConnection());
        boolean tableStillPresent = dbSupport.tableExists(dbSupport.getCurrentSchema(), "schema_version");
        assertFalse(tableStillPresent);
    }
}
