/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables.records;


import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupUser;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlywebUserGroupUserRecord extends TableRecordImpl<OrderlywebUserGroupUserRecord> implements Record2<String, String> {

    private static final long serialVersionUID = 1296963619;

    /**
     * Setter for <code>orderlyweb_user_group_user.email</code>.
     */
    public void setEmail(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>orderlyweb_user_group_user.email</code>.
     */
    public String getEmail() {
        return (String) get(0);
    }

    /**
     * Setter for <code>orderlyweb_user_group_user.user_group</code>.
     */
    public void setUserGroup(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>orderlyweb_user_group_user.user_group</code>.
     */
    public String getUserGroup() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return OrderlywebUserGroupUser.ORDERLYWEB_USER_GROUP_USER.EMAIL;
    }

    @Override
    public Field<String> field2() {
        return OrderlywebUserGroupUser.ORDERLYWEB_USER_GROUP_USER.USER_GROUP;
    }

    @Override
    public String component1() {
        return getEmail();
    }

    @Override
    public String component2() {
        return getUserGroup();
    }

    @Override
    public String value1() {
        return getEmail();
    }

    @Override
    public String value2() {
        return getUserGroup();
    }

    @Override
    public OrderlywebUserGroupUserRecord value1(String value) {
        setEmail(value);
        return this;
    }

    @Override
    public OrderlywebUserGroupUserRecord value2(String value) {
        setUserGroup(value);
        return this;
    }

    @Override
    public OrderlywebUserGroupUserRecord values(String value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OrderlywebUserGroupUserRecord
     */
    public OrderlywebUserGroupUserRecord() {
        super(OrderlywebUserGroupUser.ORDERLYWEB_USER_GROUP_USER);
    }

    /**
     * Create a detached, initialised OrderlywebUserGroupUserRecord
     */
    public OrderlywebUserGroupUserRecord(String email, String userGroup) {
        super(OrderlywebUserGroupUser.ORDERLYWEB_USER_GROUP_USER);

        set(0, email);
        set(1, userGroup);
    }
}
