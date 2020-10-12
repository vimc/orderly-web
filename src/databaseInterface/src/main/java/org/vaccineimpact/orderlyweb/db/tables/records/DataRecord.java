/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.Data;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DataRecord extends UpdatableRecordImpl<DataRecord> implements Record3<String, Long, Long> {

    private static final long serialVersionUID = 1536084576;

    /**
     * Setter for <code>data.hash</code>.
     */
    public void setHash(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>data.hash</code>.
     */
    public String getHash() {
        return (String) get(0);
    }

    /**
     * Setter for <code>data.size_csv</code>.
     */
    public void setSizeCsv(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>data.size_csv</code>.
     */
    public Long getSizeCsv() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>data.size_rds</code>.
     */
    public void setSizeRds(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>data.size_rds</code>.
     */
    public Long getSizeRds() {
        return (Long) get(2);
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
    public Row3<String, Long, Long> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, Long, Long> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return Data.DATA.HASH;
    }

    @Override
    public Field<Long> field2() {
        return Data.DATA.SIZE_CSV;
    }

    @Override
    public Field<Long> field3() {
        return Data.DATA.SIZE_RDS;
    }

    @Override
    public String component1() {
        return getHash();
    }

    @Override
    public Long component2() {
        return getSizeCsv();
    }

    @Override
    public Long component3() {
        return getSizeRds();
    }

    @Override
    public String value1() {
        return getHash();
    }

    @Override
    public Long value2() {
        return getSizeCsv();
    }

    @Override
    public Long value3() {
        return getSizeRds();
    }

    @Override
    public DataRecord value1(String value) {
        setHash(value);
        return this;
    }

    @Override
    public DataRecord value2(Long value) {
        setSizeCsv(value);
        return this;
    }

    @Override
    public DataRecord value3(Long value) {
        setSizeRds(value);
        return this;
    }

    @Override
    public DataRecord values(String value1, Long value2, Long value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached DataRecord
     */
    public DataRecord() {
        super(Data.DATA);
    }

    /**
     * Create a detached, initialised DataRecord
     */
    public DataRecord(String hash, Long sizeCsv, Long sizeRds) {
        super(Data.DATA);

        set(0, hash);
        set(1, sizeCsv);
        set(2, sizeRds);
    }
}
