/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables.records;


import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupVersionPermission;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlywebUserGroupVersionPermissionRecord extends TableRecordImpl<OrderlywebUserGroupVersionPermissionRecord> implements Record2<Integer, String> {

    private static final long serialVersionUID = -1577075118;

    /**
     * Setter for <code>orderlyweb_user_group_version_permission.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>orderlyweb_user_group_version_permission.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>orderlyweb_user_group_version_permission.version</code>.
     */
    public void setVersion(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>orderlyweb_user_group_version_permission.version</code>.
     */
    public String getVersion() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Integer, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return OrderlywebUserGroupVersionPermission.ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.ID;
    }

    @Override
    public Field<String> field2() {
        return OrderlywebUserGroupVersionPermission.ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.VERSION;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getVersion();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getVersion();
    }

    @Override
    public OrderlywebUserGroupVersionPermissionRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public OrderlywebUserGroupVersionPermissionRecord value2(String value) {
        setVersion(value);
        return this;
    }

    @Override
    public OrderlywebUserGroupVersionPermissionRecord values(Integer value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OrderlywebUserGroupVersionPermissionRecord
     */
    public OrderlywebUserGroupVersionPermissionRecord() {
        super(OrderlywebUserGroupVersionPermission.ORDERLYWEB_USER_GROUP_VERSION_PERMISSION);
    }

    /**
     * Create a detached, initialised OrderlywebUserGroupVersionPermissionRecord
     */
    public OrderlywebUserGroupVersionPermissionRecord(Integer id, String version) {
        super(OrderlywebUserGroupVersionPermission.ORDERLYWEB_USER_GROUP_VERSION_PERMISSION);

        set(0, id);
        set(1, version);
    }
}
