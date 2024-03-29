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
import org.jooq.Row9;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.vaccineimpact.orderlyweb.db.DefaultSchema;
import org.vaccineimpact.orderlyweb.db.Keys;
import org.vaccineimpact.orderlyweb.db.tables.records.OrderlywebWorkflowRunRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlywebWorkflowRun extends TableImpl<OrderlywebWorkflowRunRecord> {

    private static final long serialVersionUID = 1090014044;

    /**
     * The reference instance of <code>orderlyweb_workflow_run</code>
     */
    public static final OrderlywebWorkflowRun ORDERLYWEB_WORKFLOW_RUN = new OrderlywebWorkflowRun();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrderlywebWorkflowRunRecord> getRecordType() {
        return OrderlywebWorkflowRunRecord.class;
    }

    /**
     * The column <code>orderlyweb_workflow_run.id</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, Integer> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>orderlyweb_workflow_run.name</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_workflow_run.key</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, String> KEY = createField(DSL.name("key"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_workflow_run.email</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, String> EMAIL = createField(DSL.name("email"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_workflow_run.date</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, Timestamp> DATE = createField(DSL.name("date"), org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_workflow_run.instances</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, String> INSTANCES = createField(DSL.name("instances"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_workflow_run.git_branch</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, String> GIT_BRANCH = createField(DSL.name("git_branch"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_workflow_run.git_commit</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, String> GIT_COMMIT = createField(DSL.name("git_commit"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_workflow_run.status</code>.
     */
    public final TableField<OrderlywebWorkflowRunRecord, String> STATUS = createField(DSL.name("status"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>orderlyweb_workflow_run</code> table reference
     */
    public OrderlywebWorkflowRun() {
        this(DSL.name("orderlyweb_workflow_run"), null);
    }

    /**
     * Create an aliased <code>orderlyweb_workflow_run</code> table reference
     */
    public OrderlywebWorkflowRun(String alias) {
        this(DSL.name(alias), ORDERLYWEB_WORKFLOW_RUN);
    }

    /**
     * Create an aliased <code>orderlyweb_workflow_run</code> table reference
     */
    public OrderlywebWorkflowRun(Name alias) {
        this(alias, ORDERLYWEB_WORKFLOW_RUN);
    }

    private OrderlywebWorkflowRun(Name alias, Table<OrderlywebWorkflowRunRecord> aliased) {
        this(alias, aliased, null);
    }

    private OrderlywebWorkflowRun(Name alias, Table<OrderlywebWorkflowRunRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> OrderlywebWorkflowRun(Table<O> child, ForeignKey<O, OrderlywebWorkflowRunRecord> key) {
        super(child, key, ORDERLYWEB_WORKFLOW_RUN);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<OrderlywebWorkflowRunRecord> getPrimaryKey() {
        return Keys.PK_ORDERLYWEB_WORKFLOW_RUN;
    }

    @Override
    public List<UniqueKey<OrderlywebWorkflowRunRecord>> getKeys() {
        return Arrays.<UniqueKey<OrderlywebWorkflowRunRecord>>asList(Keys.PK_ORDERLYWEB_WORKFLOW_RUN, Keys.SQLITE_AUTOINDEX_ORDERLYWEB_WORKFLOW_RUN_2, Keys.SQLITE_AUTOINDEX_ORDERLYWEB_WORKFLOW_RUN_1);
    }

    @Override
    public List<ForeignKey<OrderlywebWorkflowRunRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<OrderlywebWorkflowRunRecord, ?>>asList(Keys.FK_ORDERLYWEB_WORKFLOW_RUN_ORDERLYWEB_USER_1);
    }

    public OrderlywebUser orderlywebUser() {
        return new OrderlywebUser(this, Keys.FK_ORDERLYWEB_WORKFLOW_RUN_ORDERLYWEB_USER_1);
    }

    @Override
    public OrderlywebWorkflowRun as(String alias) {
        return new OrderlywebWorkflowRun(DSL.name(alias), this);
    }

    @Override
    public OrderlywebWorkflowRun as(Name alias) {
        return new OrderlywebWorkflowRun(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebWorkflowRun rename(String name) {
        return new OrderlywebWorkflowRun(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebWorkflowRun rename(Name name) {
        return new OrderlywebWorkflowRun(name, null);
    }

    // -------------------------------------------------------------------------
    // Row9 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row9<Integer, String, String, String, Timestamp, String, String, String, String> fieldsRow() {
        return (Row9) super.fieldsRow();
    }
}
