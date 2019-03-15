/*
 * This file is generated by jOOQ.
*/
package org.vaccineimpact.orderlyweb.db.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionData;


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
public class ReportVersionDataRecord extends UpdatableRecordImpl<ReportVersionDataRecord> implements Record5<Integer, String, String, String, String> {

    private static final long serialVersionUID = 1714536551;

    /**
     * Setter for <code>report_version_data.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>report_version_data.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>report_version_data.report_version</code>.
     */
    public void setReportVersion(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>report_version_data.report_version</code>.
     */
    public String getReportVersion() {
        return (String) get(1);
    }

    /**
     * Setter for <code>report_version_data.name</code>.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>report_version_data.name</code>.
     */
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>report_version_data.sql</code>.
     */
    public void setSql(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>report_version_data.sql</code>.
     */
    public String getSql() {
        return (String) get(3);
    }

    /**
     * Setter for <code>report_version_data.hash</code>.
     */
    public void setHash(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>report_version_data.hash</code>.
     */
    public String getHash() {
        return (String) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Integer, String, String, String, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Integer, String, String, String, String> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return ReportVersionData.REPORT_VERSION_DATA.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return ReportVersionData.REPORT_VERSION_DATA.REPORT_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return ReportVersionData.REPORT_VERSION_DATA.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return ReportVersionData.REPORT_VERSION_DATA.SQL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return ReportVersionData.REPORT_VERSION_DATA.HASH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getReportVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getSql();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getHash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionDataRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionDataRecord value2(String value) {
        setReportVersion(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionDataRecord value3(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionDataRecord value4(String value) {
        setSql(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionDataRecord value5(String value) {
        setHash(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionDataRecord values(Integer value1, String value2, String value3, String value4, String value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ReportVersionDataRecord
     */
    public ReportVersionDataRecord() {
        super(ReportVersionData.REPORT_VERSION_DATA);
    }

    /**
     * Create a detached, initialised ReportVersionDataRecord
     */
    public ReportVersionDataRecord(Integer id, String reportVersion, String name, String sql, String hash) {
        super(ReportVersionData.REPORT_VERSION_DATA);

        set(0, id);
        set(1, reportVersion);
        set(2, name);
        set(3, sql);
        set(4, hash);
    }
}
