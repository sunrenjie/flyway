/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DB2 Support.
 */
public class DB2DbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DB2DbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 9 || (majorVersion == 9 && minorVersion < 7)) {
            throw new FlywayDbUpgradeRequiredException("DB2", version, "9.7");
        }

        if (majorVersion == 9 || (majorVersion == 10 && minorVersion < 5)) {
        throw new org.flywaydb.core.internal.dbsupport.FlywayEnterpriseUpgradeRequiredException("IBM", "DB2", version);
        }

        if (majorVersion > 11 || (majorVersion == 11 && minorVersion > 1)) {
            recommendFlywayUpgrade("DB2", version);
        }
    }

    @Override
    public String getCreateScript() {
        return "CREATE TABLE \"${schema}\".\"${table}\" (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" SMALLINT NOT NULL,\n" +
                "    CONSTRAINT \"${table}_s\" CHECK (\"success\" in(0,1))\n" +



                        ") ORGANIZE BY ROW;\n"



                + "ALTER TABLE \"${schema}\".\"${table}\" ADD CONSTRAINT \"${table}_pk\" PRIMARY KEY (\"installed_rank\");\n" +
                "\n" +
                "CREATE INDEX \"${schema}\".\"${table}_s_idx\" ON \"${schema}\".\"${table}\" (\"success\");";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new DB2SqlStatementBuilder();
    }

    public String getDbName() {
        return "db2";
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1");
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + quote(schema));
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new DB2Schema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }
}