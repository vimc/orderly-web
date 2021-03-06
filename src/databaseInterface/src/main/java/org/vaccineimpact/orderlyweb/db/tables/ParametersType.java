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
import org.vaccineimpact.orderlyweb.db.tables.records.ParametersTypeRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ParametersType extends TableImpl<ParametersTypeRecord> {

    private static final long serialVersionUID = 671520627;

    /**
     * The reference instance of <code>parameters_type</code>
     */
    public static final ParametersType PARAMETERS_TYPE = new ParametersType();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ParametersTypeRecord> getRecordType() {
        return ParametersTypeRecord.class;
    }

    /**
     * The column <code>parameters_type.name</code>.
     */
    public final TableField<ParametersTypeRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>parameters_type</code> table reference
     */
    public ParametersType() {
        this(DSL.name("parameters_type"), null);
    }

    /**
     * Create an aliased <code>parameters_type</code> table reference
     */
    public ParametersType(String alias) {
        this(DSL.name(alias), PARAMETERS_TYPE);
    }

    /**
     * Create an aliased <code>parameters_type</code> table reference
     */
    public ParametersType(Name alias) {
        this(alias, PARAMETERS_TYPE);
    }

    private ParametersType(Name alias, Table<ParametersTypeRecord> aliased) {
        this(alias, aliased, null);
    }

    private ParametersType(Name alias, Table<ParametersTypeRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> ParametersType(Table<O> child, ForeignKey<O, ParametersTypeRecord> key) {
        super(child, key, PARAMETERS_TYPE);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<ParametersTypeRecord> getPrimaryKey() {
        return Keys.PK_PARAMETERS_TYPE;
    }

    @Override
    public List<UniqueKey<ParametersTypeRecord>> getKeys() {
        return Arrays.<UniqueKey<ParametersTypeRecord>>asList(Keys.PK_PARAMETERS_TYPE);
    }

    @Override
    public ParametersType as(String alias) {
        return new ParametersType(DSL.name(alias), this);
    }

    @Override
    public ParametersType as(Name alias) {
        return new ParametersType(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ParametersType rename(String name) {
        return new ParametersType(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ParametersType rename(Name name) {
        return new ParametersType(name, null);
    }

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row1<String> fieldsRow() {
        return (Row1) super.fieldsRow();
    }
}
