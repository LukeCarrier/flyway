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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * A migration of a single version of the schema.
 *
 * @author Axel Fontaine
 */
public abstract class Migration implements Comparable<Migration> {
    /**
     * The target schema version of this migration.
     */
    protected SchemaVersion schemaVersion = SchemaVersion.EMPTY;

    /**
     * The description for the migration history.
     */
    protected String description;

    /**
     * The script name for the migration history.
     */
    protected String script;

    /**
     * The checksum of the migration. Sql migrations use a crc-32 checksum of the sql script. Java migrations use a
     * custom checksum.
     */
    protected Integer checksum;

    /**
     * @return The type of migration (INIT, SQL or JAVA)
     */
    public abstract MigrationType getMigrationType();

    /**
     * @return The checksum of the migration.
     */
    public Integer getChecksum() {
        return checksum;
    }

    /**
     * @return The schema version after the migration is complete.
     */
    public SchemaVersion getVersion() {
        return schemaVersion;
    }

    /**
     * @return The description for the migration history.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The script name for the migration history.
     */
    public String getScript() {
        return script;
    }

    public int compareTo(Migration o) {
        return getVersion().compareTo(o.getVersion());
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Migration)) return false;

        Migration migration = (Migration) o;

        if (checksum != null ? !checksum.equals(migration.checksum) : migration.checksum != null) return false;
        if (description != null ? !description.equals(migration.description) : migration.description != null)
            return false;
        return !(schemaVersion != null ? !schemaVersion.equals(migration.schemaVersion) : migration.schemaVersion != null) && !(script != null ? !script.equals(migration.script) : migration.script != null);
    }

    @Override
    public int hashCode() {
        int result = schemaVersion != null ? schemaVersion.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (script != null ? script.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        return result;
    }

    /**
     * Performs the migration.
     *
     * @param jdbcTemplate To execute the migration statements.
     * @param dbSupport    The support for database-specific extensions.
     * @throws SQLException Thrown when the migration failed.
     */
    public abstract void migrate(JdbcTemplate jdbcTemplate, DbSupport dbSupport) throws SQLException;

    /**
     * retrieves the location of the migration
     * @return source of this migration
     */
    public abstract String getLocation();
}
