/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Row1;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.ParametersType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ParametersTypeRecord extends UpdatableRecordImpl<ParametersTypeRecord> implements Record1<String> {

    private static final long serialVersionUID = -2072550778;

    /**
     * Setter for <code>parameters_type.name</code>.
     */
    public void setName(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>parameters_type.name</code>.
     */
    public String getName() {
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
        return ParametersType.PARAMETERS_TYPE.NAME;
    }

    @Override
    public String component1() {
        return getName();
    }

    @Override
    public String value1() {
        return getName();
    }

    @Override
    public ParametersTypeRecord value1(String value) {
        setName(value);
        return this;
    }

    @Override
    public ParametersTypeRecord values(String value1) {
        value1(value1);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ParametersTypeRecord
     */
    public ParametersTypeRecord() {
        super(ParametersType.PARAMETERS_TYPE);
    }

    /**
     * Create a detached, initialised ParametersTypeRecord
     */
    public ParametersTypeRecord(String name) {
        super(ParametersType.PARAMETERS_TYPE);

        set(0, name);
    }
}
