/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row12;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.vaccineimpact.orderlyweb.db.DefaultSchema;
import org.vaccineimpact.orderlyweb.db.Keys;
import org.vaccineimpact.orderlyweb.db.tables.records.OrderlywebReportRunRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlywebReportRun extends TableImpl<OrderlywebReportRunRecord> {

    private static final long serialVersionUID = -729066364;

    /**
     * The reference instance of <code>orderlyweb_report_run</code>
     */
    public static final OrderlywebReportRun ORDERLYWEB_REPORT_RUN = new OrderlywebReportRun();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrderlywebReportRunRecord> getRecordType() {
        return OrderlywebReportRunRecord.class;
    }

    /**
     * The column <code>orderlyweb_report_run.id</code>.
     */
    public final TableField<OrderlywebReportRunRecord, Integer> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>orderlyweb_report_run.key</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> KEY = createField(DSL.name("key"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_report_run.email</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> EMAIL = createField(DSL.name("email"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_report_run.date</code>.
     */
    public final TableField<OrderlywebReportRunRecord, Timestamp> DATE = createField(DSL.name("date"), org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_report_run.report</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> REPORT = createField(DSL.name("report"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_report_run.instances</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> INSTANCES = createField(DSL.name("instances"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_report_run.params</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> PARAMS = createField(DSL.name("params"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_report_run.git_branch</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> GIT_BRANCH = createField(DSL.name("git_branch"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_report_run.git_commit</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> GIT_COMMIT = createField(DSL.name("git_commit"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_report_run.status</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> STATUS = createField(DSL.name("status"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_report_run.logs</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> LOGS = createField(DSL.name("logs"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_report_run.report_version</code>.
     */
    public final TableField<OrderlywebReportRunRecord, String> REPORT_VERSION = createField(DSL.name("report_version"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>orderlyweb_report_run</code> table reference
     */
    public OrderlywebReportRun() {
        this(DSL.name("orderlyweb_report_run"), null);
    }

    /**
     * Create an aliased <code>orderlyweb_report_run</code> table reference
     */
    public OrderlywebReportRun(String alias) {
        this(DSL.name(alias), ORDERLYWEB_REPORT_RUN);
    }

    /**
     * Create an aliased <code>orderlyweb_report_run</code> table reference
     */
    public OrderlywebReportRun(Name alias) {
        this(alias, ORDERLYWEB_REPORT_RUN);
    }

    private OrderlywebReportRun(Name alias, Table<OrderlywebReportRunRecord> aliased) {
        this(alias, aliased, null);
    }

    private OrderlywebReportRun(Name alias, Table<OrderlywebReportRunRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> OrderlywebReportRun(Table<O> child, ForeignKey<O, OrderlywebReportRunRecord> key) {
        super(child, key, ORDERLYWEB_REPORT_RUN);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<OrderlywebReportRunRecord> getPrimaryKey() {
        return Keys.PK_ORDERLYWEB_REPORT_RUN;
    }

    @Override
    public List<UniqueKey<OrderlywebReportRunRecord>> getKeys() {
        return Arrays.<UniqueKey<OrderlywebReportRunRecord>>asList(Keys.PK_ORDERLYWEB_REPORT_RUN);
    }

    @Override
    public List<ForeignKey<OrderlywebReportRunRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<OrderlywebReportRunRecord, ?>>asList(Keys.FK_ORDERLYWEB_REPORT_RUN_ORDERLYWEB_USER_1, Keys.FK_ORDERLYWEB_REPORT_RUN_REPORT_VERSION_1);
    }

    public OrderlywebUser orderlywebUser() {
        return new OrderlywebUser(this, Keys.FK_ORDERLYWEB_REPORT_RUN_ORDERLYWEB_USER_1);
    }

    public ReportVersion reportVersion() {
        return new ReportVersion(this, Keys.FK_ORDERLYWEB_REPORT_RUN_REPORT_VERSION_1);
    }

    @Override
    public OrderlywebReportRun as(String alias) {
        return new OrderlywebReportRun(DSL.name(alias), this);
    }

    @Override
    public OrderlywebReportRun as(Name alias) {
        return new OrderlywebReportRun(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebReportRun rename(String name) {
        return new OrderlywebReportRun(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebReportRun rename(Name name) {
        return new OrderlywebReportRun(name, null);
    }

    // -------------------------------------------------------------------------
    // Row12 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row12<Integer, String, String, Timestamp, String, String, String, String, String, String, String, String> fieldsRow() {
        return (Row12) super.fieldsRow();
    }
}
