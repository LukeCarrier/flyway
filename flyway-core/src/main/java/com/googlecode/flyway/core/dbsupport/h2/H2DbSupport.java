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

package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.runtime.SqlScript;
import com.googlecode.flyway.core.runtime.SqlStatement;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * H2 database specific support
 */
public class H2DbSupport implements DbSupport {
    @Override
    public String getCreateMetaDataTableScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/h2/createMetaDataTable.sql";
    }

    @Override
    public String getCurrentSchema(JdbcTemplate jdbcTemplate) {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getSchemas();
                while (resultSet.next()) {
                    if (resultSet.getBoolean("IS_DEFAULT")) {
                        return resultSet.getString("TABLE_SCHEM");
                    }
                }
                return null;
            }
        });
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "H2".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(final JdbcTemplate jdbcTemplate, final String schemaMetaDataTable) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, getCurrentSchema(jdbcTemplate),
                        schemaMetaDataTable.toUpperCase(), null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsLocking() {
        return true;
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new SqlScript(sqlScriptSource, placeholderReplacer);
    }

    @Override
    public SqlScript createCleanScript(JdbcTemplate jdbcTemplate) {
        return new SqlScript(new ArrayList<SqlStatement>());
    }
}