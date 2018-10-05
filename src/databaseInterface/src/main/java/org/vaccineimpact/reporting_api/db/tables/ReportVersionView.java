/*
 * This file is generated by jOOQ.
*/
package org.vaccineimpact.reporting_api.db.tables;


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
import org.vaccineimpact.reporting_api.db.tables.records.ReportVersionViewRecord;


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
public class ReportVersionView extends TableImpl<ReportVersionViewRecord> {

    private static final long serialVersionUID = -1258735392;

    /**
     * The reference instance of <code>report_version_view</code>
     */
    public static final ReportVersionView REPORT_VERSION_VIEW = new ReportVersionView();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ReportVersionViewRecord> getRecordType() {
        return ReportVersionViewRecord.class;
    }

    /**
     * The column <code>report_version_view.id</code>.
     */
    public final TableField<ReportVersionViewRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>report_version_view.report_version</code>.
     */
    public final TableField<ReportVersionViewRecord, String> REPORT_VERSION = createField("report_version", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>report_version_view.name</code>.
     */
    public final TableField<ReportVersionViewRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>report_version_view.sql</code>.
     */
    public final TableField<ReportVersionViewRecord, String> SQL = createField("sql", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * Create a <code>report_version_view</code> table reference
     */
    public ReportVersionView() {
        this("report_version_view", null);
    }

    /**
     * Create an aliased <code>report_version_view</code> table reference
     */
    public ReportVersionView(String alias) {
        this(alias, REPORT_VERSION_VIEW);
    }

    private ReportVersionView(String alias, Table<ReportVersionViewRecord> aliased) {
        this(alias, aliased, null);
    }

    private ReportVersionView(String alias, Table<ReportVersionViewRecord> aliased, Field<?>[] parameters) {
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
    public UniqueKey<ReportVersionViewRecord> getPrimaryKey() {
        return Keys.PK_REPORT_VERSION_VIEW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ReportVersionViewRecord>> getKeys() {
        return Arrays.<UniqueKey<ReportVersionViewRecord>>asList(Keys.PK_REPORT_VERSION_VIEW);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ReportVersionViewRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ReportVersionViewRecord, ?>>asList(Keys.FK_REPORT_VERSION_VIEW_REPORT_VERSION_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionView as(String alias) {
        return new ReportVersionView(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ReportVersionView rename(String name) {
        return new ReportVersionView(name, null);
    }
}
