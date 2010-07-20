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

import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Migration resolver for sql files on the classpath.
 * The sql files must have names like V1.sql or V1_1.sql or V1__Description.sql or V1_1__Description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver {
    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(SqlMigrationResolver.class);

    /**
     * Spring utility for loading resources from the classpath using wildcards.
     */
    private final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver =
            new PathMatchingResourcePatternResolver();

    /**
     * The base directory on the classpath where to migrations are located.
     */
    private final String baseDir;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     */
    private final Map<String, String> placeholders;

    /**
     * The encoding of Sql migrations.
     */
    private final String encoding;

    /**
     * Creates a new instance.
     *
     * @param baseDir      The base directory on the classpath where to migrations are located.
     * @param placeholders A map of <placeholder, replacementValue> to apply to sql migration scripts.
     * @param encoding     The encoding of Sql migrations.
     */
    public SqlMigrationResolver(String baseDir, Map<String, String> placeholders, String encoding) {
        this.baseDir = baseDir;
        this.placeholders = placeholders;
        this.encoding = encoding;
    }

    @Override
    public Collection<Migration> resolvesMigrations() {
        Collection<Migration> migrations = new ArrayList<Migration>();

        Resource classPathBaseDir = new ClassPathResource(baseDir + "/");
        if (!classPathBaseDir.exists()) {
            log.warn("Unable to find path for sql migrations: " + baseDir);
            return migrations;
        }

        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath:" + baseDir + "/V?*.sql");
            for (Resource resource : resources) {
                migrations.add(new SqlMigration(resource, placeholders, encoding));
            }
        } catch (IOException e) {
            log.error("Error loading sql migration files", e);
        }

        return migrations;
    }
}