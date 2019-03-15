/*
 * This file is generated by jOOQ.
*/
package org.vaccineimpact.orderlyweb.db.tables;


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
import org.vaccineimpact.orderlyweb.db.DefaultSchema;
import org.vaccineimpact.orderlyweb.db.Keys;
import org.vaccineimpact.orderlyweb.db.tables.records.OrderlywebUserGroupPermissionRecord;


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
public class OrderlywebUserGroupPermission extends TableImpl<OrderlywebUserGroupPermissionRecord> {

    private static final long serialVersionUID = 395804206;

    /**
     * The reference instance of <code>orderlyweb_user_group_permission</code>
     */
    public static final OrderlywebUserGroupPermission ORDERLYWEB_USER_GROUP_PERMISSION = new OrderlywebUserGroupPermission();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrderlywebUserGroupPermissionRecord> getRecordType() {
        return OrderlywebUserGroupPermissionRecord.class;
    }

    /**
     * The column <code>orderlyweb_user_group_permission.id</code>.
     */
    public final TableField<OrderlywebUserGroupPermissionRecord, Object> ID = createField("id", org.jooq.impl.DefaultDataType.getDefaultDataType("SERIAL"), this, "");

    /**
     * The column <code>orderlyweb_user_group_permission.user_group</code>.
     */
    public final TableField<OrderlywebUserGroupPermissionRecord, String> USER_GROUP = createField("user_group", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>orderlyweb_user_group_permission.permission</code>.
     */
    public final TableField<OrderlywebUserGroupPermissionRecord, String> PERMISSION = createField("permission", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * Create a <code>orderlyweb_user_group_permission</code> table reference
     */
    public OrderlywebUserGroupPermission() {
        this("orderlyweb_user_group_permission", null);
    }

    /**
     * Create an aliased <code>orderlyweb_user_group_permission</code> table reference
     */
    public OrderlywebUserGroupPermission(String alias) {
        this(alias, ORDERLYWEB_USER_GROUP_PERMISSION);
    }

    private OrderlywebUserGroupPermission(String alias, Table<OrderlywebUserGroupPermissionRecord> aliased) {
        this(alias, aliased, null);
    }

    private OrderlywebUserGroupPermission(String alias, Table<OrderlywebUserGroupPermissionRecord> aliased, Field<?>[] parameters) {
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
    public UniqueKey<OrderlywebUserGroupPermissionRecord> getPrimaryKey() {
        return Keys.PK_ORDERLYWEB_USER_GROUP_PERMISSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<OrderlywebUserGroupPermissionRecord>> getKeys() {
        return Arrays.<UniqueKey<OrderlywebUserGroupPermissionRecord>>asList(Keys.PK_ORDERLYWEB_USER_GROUP_PERMISSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<OrderlywebUserGroupPermissionRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<OrderlywebUserGroupPermissionRecord, ?>>asList(Keys.FK_ORDERLYWEB_USER_GROUP_PERMISSION_ORDERLYWEB_USER_GROUP_1, Keys.FK_ORDERLYWEB_USER_GROUP_PERMISSION_ORDERLYWEB_PERMISSION_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderlywebUserGroupPermission as(String alias) {
        return new OrderlywebUserGroupPermission(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderlywebUserGroupPermission rename(String name) {
        return new OrderlywebUserGroupPermission(name, null);
    }
}
