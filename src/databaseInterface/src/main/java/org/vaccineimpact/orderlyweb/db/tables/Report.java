/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables;


import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.vaccineimpact.orderlyweb.db.DefaultSchema;
import org.vaccineimpact.orderlyweb.db.Keys;
import org.vaccineimpact.orderlyweb.db.tables.records.ReportRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Report extends TableImpl<ReportRecord> {

    private static final long serialVersionUID = -144858553;

    /**
     * The reference instance of <code>report</code>
     */
    public static final Report REPORT = new Report();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ReportRecord> getRecordType() {
        return ReportRecord.class;
    }

    /**
     * The column <code>report.name</code>.
     */
    public final TableField<ReportRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>report.latest</code>.
     */
    public final TableField<ReportRecord, String> LATEST = createField(DSL.name("latest"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>report</code> table reference
     */
    public Report() {
        this(DSL.name("report"), null);
    }

    /**
     * Create an aliased <code>report</code> table reference
     */
    public Report(String alias) {
        this(DSL.name(alias), REPORT);
    }

    /**
     * Create an aliased <code>report</code> table reference
     */
    public Report(Name alias) {
        this(alias, REPORT);
    }

    private Report(Name alias, Table<ReportRecord> aliased) {
        this(alias, aliased, null);
    }

    private Report(Name alias, Table<ReportRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> Report(Table<O> child, ForeignKey<O, ReportRecord> key) {
        super(child, key, REPORT);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<ReportRecord> getPrimaryKey() {
        return Keys.PK_REPORT;
    }

    @Override
    public List<UniqueKey<ReportRecord>> getKeys() {
        return Arrays.<UniqueKey<ReportRecord>>asList(Keys.PK_REPORT);
    }

    @Override
    public List<ForeignKey<ReportRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ReportRecord, ?>>asList(Keys.FK_REPORT_REPORT_VERSION_1);
    }

    public ReportVersion reportVersion() {
        return new ReportVersion(this, Keys.FK_REPORT_REPORT_VERSION_1);
    }

    @Override
    public Report as(String alias) {
        return new Report(DSL.name(alias), this);
    }

    @Override
    public Report as(Name alias) {
        return new Report(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Report rename(String name) {
        return new Report(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Report rename(Name name) {
        return new Report(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
