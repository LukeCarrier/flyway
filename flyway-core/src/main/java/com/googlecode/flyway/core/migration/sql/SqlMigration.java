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

package com.googlecode.flyway.core.migration.sql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.BaseMigration;
import com.googlecode.flyway.core.runtime.SqlScript;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration extends BaseMigration {
    /**
     * The resource containing the sql script.
     */
    private final Resource sqlScriptResource;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration
     * scripts.
     */
    private final Map<String, String> placeholders;

    /**
     * The encoding of this Sql migration.
     */
    private final String encoding;

    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param sqlScriptResource The resource containing the sql script.
     * @param placeholders      The placeholders to replace in the sql script.
     * @param encoding          The encoding of this Sql migration.
     */
    public SqlMigration(Resource sqlScriptResource, Map<String, String> placeholders, String encoding) {
        initVersion(extractVersionStringFromFileName(sqlScriptResource.getFilename()));
        scriptName = "Sql File: " + sqlScriptResource.getFilename();

        this.sqlScriptResource = sqlScriptResource;
        this.placeholders = placeholders;
        this.encoding = encoding;
    }

    /**
     * Extracts the sql file version string from this file name.
     *
     * @param fileName The file name to parse.
     * @return The version string.
     */
    /* private -> for testing */
    static String extractVersionStringFromFileName(String fileName) {
        int lastDirSeparator = fileName.lastIndexOf("/");
        int extension = fileName.lastIndexOf(".sql");

        return fileName.substring(lastDirSeparator + 1, extension);
    }

    @Override
    public void doMigrate(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        String sqlScriptSource = ResourceUtils.loadResourceAsString(sqlScriptResource, encoding);
        SqlScript sqlScript = dbSupport.createSqlScript(sqlScriptSource, placeholders);
        sqlScript.execute(transactionTemplate, jdbcTemplate);
    }
}