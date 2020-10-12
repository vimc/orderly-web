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
import org.jooq.Row6;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.vaccineimpact.orderlyweb.db.DefaultSchema;
import org.vaccineimpact.orderlyweb.db.Keys;
import org.vaccineimpact.orderlyweb.db.tables.records.OrderlywebUserRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlywebUser extends TableImpl<OrderlywebUserRecord> {

    private static final long serialVersionUID = -1236687307;

    /**
     * The reference instance of <code>orderlyweb_user</code>
     */
    public static final OrderlywebUser ORDERLYWEB_USER = new OrderlywebUser();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrderlywebUserRecord> getRecordType() {
        return OrderlywebUserRecord.class;
    }

    /**
     * The column <code>orderlyweb_user.username</code>.
     */
    public final TableField<OrderlywebUserRecord, String> USERNAME = createField(DSL.name("username"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_user.display_name</code>.
     */
    public final TableField<OrderlywebUserRecord, String> DISPLAY_NAME = createField(DSL.name("display_name"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_user.email</code>.
     */
    public final TableField<OrderlywebUserRecord, String> EMAIL = createField(DSL.name("email"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_user.disabled</code>.
     */
    public final TableField<OrderlywebUserRecord, Integer> DISABLED = createField(DSL.name("disabled"), org.jooq.impl.SQLDataType.INTEGER.defaultValue(org.jooq.impl.DSL.field("0", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>orderlyweb_user.user_source</code>.
     */
    public final TableField<OrderlywebUserRecord, String> USER_SOURCE = createField(DSL.name("user_source"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>orderlyweb_user.last_logged_in</code>.
     */
    public final TableField<OrderlywebUserRecord, String> LAST_LOGGED_IN = createField(DSL.name("last_logged_in"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>orderlyweb_user</code> table reference
     */
    public OrderlywebUser() {
        this(DSL.name("orderlyweb_user"), null);
    }

    /**
     * Create an aliased <code>orderlyweb_user</code> table reference
     */
    public OrderlywebUser(String alias) {
        this(DSL.name(alias), ORDERLYWEB_USER);
    }

    /**
     * Create an aliased <code>orderlyweb_user</code> table reference
     */
    public OrderlywebUser(Name alias) {
        this(alias, ORDERLYWEB_USER);
    }

    private OrderlywebUser(Name alias, Table<OrderlywebUserRecord> aliased) {
        this(alias, aliased, null);
    }

    private OrderlywebUser(Name alias, Table<OrderlywebUserRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> OrderlywebUser(Table<O> child, ForeignKey<O, OrderlywebUserRecord> key) {
        super(child, key, ORDERLYWEB_USER);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<OrderlywebUserRecord> getPrimaryKey() {
        return Keys.PK_ORDERLYWEB_USER;
    }

    @Override
    public List<UniqueKey<OrderlywebUserRecord>> getKeys() {
        return Arrays.<UniqueKey<OrderlywebUserRecord>>asList(Keys.PK_ORDERLYWEB_USER);
    }

    @Override
    public OrderlywebUser as(String alias) {
        return new OrderlywebUser(DSL.name(alias), this);
    }

    @Override
    public OrderlywebUser as(Name alias) {
        return new OrderlywebUser(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebUser rename(String name) {
        return new OrderlywebUser(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebUser rename(Name name) {
        return new OrderlywebUser(name, null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<String, String, String, Integer, String, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }
}
