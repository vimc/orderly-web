/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables.records;


import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.OrderlySchema;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderlySchemaRecord extends UpdatableRecordImpl<OrderlySchemaRecord> implements Record3<String, String, Timestamp> {

    private static final long serialVersionUID = -2127515233;

    /**
     * Setter for <code>orderly_schema.schema_version</code>.
     */
    public void setSchemaVersion(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>orderly_schema.schema_version</code>.
     */
    public String getSchemaVersion() {
        return (String) get(0);
    }

    /**
     * Setter for <code>orderly_schema.orderly_version</code>.
     */
    public void setOrderlyVersion(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>orderly_schema.orderly_version</code>.
     */
    public String getOrderlyVersion() {
        return (String) get(1);
    }

    /**
     * Setter for <code>orderly_schema.created</code>.
     */
    public void setCreated(Timestamp value) {
        set(2, value);
    }

    /**
     * Getter for <code>orderly_schema.created</code>.
     */
    public Timestamp getCreated() {
        return (Timestamp) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, Timestamp> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, String, Timestamp> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return OrderlySchema.ORDERLY_SCHEMA.SCHEMA_VERSION;
    }

    @Override
    public Field<String> field2() {
        return OrderlySchema.ORDERLY_SCHEMA.ORDERLY_VERSION;
    }

    @Override
    public Field<Timestamp> field3() {
        return OrderlySchema.ORDERLY_SCHEMA.CREATED;
    }

    @Override
    public String component1() {
        return getSchemaVersion();
    }

    @Override
    public String component2() {
        return getOrderlyVersion();
    }

    @Override
    public Timestamp component3() {
        return getCreated();
    }

    @Override
    public String value1() {
        return getSchemaVersion();
    }

    @Override
    public String value2() {
        return getOrderlyVersion();
    }

    @Override
    public Timestamp value3() {
        return getCreated();
    }

    @Override
    public OrderlySchemaRecord value1(String value) {
        setSchemaVersion(value);
        return this;
    }

    @Override
    public OrderlySchemaRecord value2(String value) {
        setOrderlyVersion(value);
        return this;
    }

    @Override
    public OrderlySchemaRecord value3(Timestamp value) {
        setCreated(value);
        return this;
    }

    @Override
    public OrderlySchemaRecord values(String value1, String value2, Timestamp value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OrderlySchemaRecord
     */
    public OrderlySchemaRecord() {
        super(OrderlySchema.ORDERLY_SCHEMA);
    }

    /**
     * Create a detached, initialised OrderlySchemaRecord
     */
    public OrderlySchemaRecord(String schemaVersion, String orderlyVersion, Timestamp created) {
        super(OrderlySchema.ORDERLY_SCHEMA);

        set(0, schemaVersion);
        set(1, orderlyVersion);
        set(2, created);
    }
}
