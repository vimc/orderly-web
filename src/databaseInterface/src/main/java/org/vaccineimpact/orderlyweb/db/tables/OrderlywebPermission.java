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
import org.jooq.Row1;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.vaccineimpact.orderlyweb.db.DefaultSchema;
import org.vaccineimpact.orderlyweb.db.Keys;
import org.vaccineimpact.orderlyweb.db.tables.records.OrderlywebPermissionRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlywebPermission extends TableImpl<OrderlywebPermissionRecord> {

    private static final long serialVersionUID = -855476561;

    /**
     * The reference instance of <code>orderlyweb_permission</code>
     */
    public static final OrderlywebPermission ORDERLYWEB_PERMISSION = new OrderlywebPermission();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrderlywebPermissionRecord> getRecordType() {
        return OrderlywebPermissionRecord.class;
    }

    /**
     * The column <code>orderlyweb_permission.id</code>.
     */
    public final TableField<OrderlywebPermissionRecord, String> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>orderlyweb_permission</code> table reference
     */
    public OrderlywebPermission() {
        this(DSL.name("orderlyweb_permission"), null);
    }

    /**
     * Create an aliased <code>orderlyweb_permission</code> table reference
     */
    public OrderlywebPermission(String alias) {
        this(DSL.name(alias), ORDERLYWEB_PERMISSION);
    }

    /**
     * Create an aliased <code>orderlyweb_permission</code> table reference
     */
    public OrderlywebPermission(Name alias) {
        this(alias, ORDERLYWEB_PERMISSION);
    }

    private OrderlywebPermission(Name alias, Table<OrderlywebPermissionRecord> aliased) {
        this(alias, aliased, null);
    }

    private OrderlywebPermission(Name alias, Table<OrderlywebPermissionRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> OrderlywebPermission(Table<O> child, ForeignKey<O, OrderlywebPermissionRecord> key) {
        super(child, key, ORDERLYWEB_PERMISSION);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<OrderlywebPermissionRecord> getPrimaryKey() {
        return Keys.PK_ORDERLYWEB_PERMISSION;
    }

    @Override
    public List<UniqueKey<OrderlywebPermissionRecord>> getKeys() {
        return Arrays.<UniqueKey<OrderlywebPermissionRecord>>asList(Keys.PK_ORDERLYWEB_PERMISSION);
    }

    @Override
    public OrderlywebPermission as(String alias) {
        return new OrderlywebPermission(DSL.name(alias), this);
    }

    @Override
    public OrderlywebPermission as(Name alias) {
        return new OrderlywebPermission(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebPermission rename(String name) {
        return new OrderlywebPermission(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebPermission rename(Name name) {
        return new OrderlywebPermission(name, null);
    }

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row1<String> fieldsRow() {
        return (Row1) super.fieldsRow();
    }
}
