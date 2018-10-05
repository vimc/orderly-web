/*
 * This file is generated by jOOQ.
*/
package org.vaccineimpact.reporting_api.db.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import org.vaccineimpact.reporting_api.db.DefaultSchema;
import org.vaccineimpact.reporting_api.db.Keys;
import org.vaccineimpact.reporting_api.db.tables.records.ReportVersionRecord;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ReportVersion extends TableImpl<ReportVersionRecord> {

    private static final long serialVersionUID = -2001815047;

    /**
     * The reference instance of <code>report_version</code>
     */
    public static final ReportVersion REPORT_VERSION = new ReportVersion();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ReportVersionRecord> getRecordType() {
        return ReportVersionRecord.class;
    }

    /**
     * The column <code>report_version.id</code>.
     */
    public final TableField<ReportVersionRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>report_version.report</code>.
     */
    public final TableField<ReportVersionRecord, String> REPORT = createField("report", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>report_version.date</code>.
     */
    public final TableField<ReportVersionRecord, Timestamp> DATE = createField("date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>report_version.displayname</code>.
     */
    public final TableField<ReportVersionRecord, String> DISPLAYNAME = createField("displayname", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>report_version.description</code>.
     */
    public final TableField<ReportVersionRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>report_version.requester</code>.
     */
    public final TableField<ReportVersionRecord, String> REQUESTER = createField("requester", org.jooq.impl.SQLDataType.CHAR.nullable(false), this, "");

    /**
     * The column <code>report_version.author</code>.
     */
    public final TableField<ReportVersionRecord, String> AUTHOR = createField("author", org.jooq.impl.SQLDataType.CHAR.nullable(false), this, "");

    /**
     * The column <code>report_version.comment</code>.
     */
    public final TableField<ReportVersionRecord, String> COMMENT = createField("comment", org.jooq.impl.SQLDataType.CHAR, this, "");

    /**
     * Create a <code>report_version</code> table reference
     */
    public ReportVersion() {
        this("report_version", null);
    }

    /**
     * Create an aliased <code>report_version</code> table reference
     */
    public ReportVersion(String alias) {
        this(alias, REPORT_VERSION);
    }

    private ReportVersion(String alias, Table<ReportVersionRecord> aliased) {
        this(alias, aliased, null);
    }

    private ReportVersion(String alias, Table<ReportVersionRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ReportVersionRecord> getPrimaryKey() {
        return Keys.PK_REPORT_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ReportVersionRecord>> getKeys() {
        return Arrays.<UniqueKey<ReportVersionRecord>>asList(Keys.PK_REPORT_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ReportVersionRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ReportVersionRecord, ?>>asList(Keys.FK_REPORT_VERSION_REPORT_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersion as(String alias) {
        return new ReportVersion(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ReportVersion rename(String name) {
        return new ReportVersion(name, null);
    }
}
