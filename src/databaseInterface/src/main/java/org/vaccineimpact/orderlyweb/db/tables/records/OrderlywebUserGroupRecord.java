/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Row1;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroup;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlywebUserGroupRecord extends UpdatableRecordImpl<OrderlywebUserGroupRecord> implements Record1<String> {

    private static final long serialVersionUID = 580361014;

    /**
     * Setter for <code>orderlyweb_user_group.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>orderlyweb_user_group.id</code>.
     */
    public String getId() {
        return (String) get(0);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record1 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row1<String> fieldsRow() {
        return (Row1) super.fieldsRow();
    }

    @Override
    public Row1<String> valuesRow() {
        return (Row1) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return OrderlywebUserGroup.ORDERLYWEB_USER_GROUP.ID;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public OrderlywebUserGroupRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public OrderlywebUserGroupRecord values(String value1) {
        value1(value1);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OrderlywebUserGroupRecord
     */
    public OrderlywebUserGroupRecord() {
        super(OrderlywebUserGroup.ORDERLYWEB_USER_GROUP);
    }

    /**
     * Create a detached, initialised OrderlywebUserGroupRecord
     */
    public OrderlywebUserGroupRecord(String id) {
        super(OrderlywebUserGroup.ORDERLYWEB_USER_GROUP);

        set(0, id);
    }
}
