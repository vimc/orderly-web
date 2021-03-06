/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionData;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ReportVersionDataRecord extends UpdatableRecordImpl<ReportVersionDataRecord> implements Record6<Integer, String, String, String, String, String> {

    private static final long serialVersionUID = 1202310783;

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
     * Setter for <code>report_version_data.database</code>.
     */
    public void setDatabase(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>report_version_data.database</code>.
     */
    public String getDatabase() {
        return (String) get(3);
    }

    /**
     * Setter for <code>report_version_data.query</code>.
     */
    public void setQuery(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>report_version_data.query</code>.
     */
    public String getQuery() {
        return (String) get(4);
    }

    /**
     * Setter for <code>report_version_data.hash</code>.
     */
    public void setHash(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>report_version_data.hash</code>.
     */
    public String getHash() {
        return (String) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<Integer, String, String, String, String, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<Integer, String, String, String, String, String> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return ReportVersionData.REPORT_VERSION_DATA.ID;
    }

    @Override
    public Field<String> field2() {
        return ReportVersionData.REPORT_VERSION_DATA.REPORT_VERSION;
    }

    @Override
    public Field<String> field3() {
        return ReportVersionData.REPORT_VERSION_DATA.NAME;
    }

    @Override
    public Field<String> field4() {
        return ReportVersionData.REPORT_VERSION_DATA.DATABASE;
    }

    @Override
    public Field<String> field5() {
        return ReportVersionData.REPORT_VERSION_DATA.QUERY;
    }

    @Override
    public Field<String> field6() {
        return ReportVersionData.REPORT_VERSION_DATA.HASH;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getReportVersion();
    }

    @Override
    public String component3() {
        return getName();
    }

    @Override
    public String component4() {
        return getDatabase();
    }

    @Override
    public String component5() {
        return getQuery();
    }

    @Override
    public String component6() {
        return getHash();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getReportVersion();
    }

    @Override
    public String value3() {
        return getName();
    }

    @Override
    public String value4() {
        return getDatabase();
    }

    @Override
    public String value5() {
        return getQuery();
    }

    @Override
    public String value6() {
        return getHash();
    }

    @Override
    public ReportVersionDataRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public ReportVersionDataRecord value2(String value) {
        setReportVersion(value);
        return this;
    }

    @Override
    public ReportVersionDataRecord value3(String value) {
        setName(value);
        return this;
    }

    @Override
    public ReportVersionDataRecord value4(String value) {
        setDatabase(value);
        return this;
    }

    @Override
    public ReportVersionDataRecord value5(String value) {
        setQuery(value);
        return this;
    }

    @Override
    public ReportVersionDataRecord value6(String value) {
        setHash(value);
        return this;
    }

    @Override
    public ReportVersionDataRecord values(Integer value1, String value2, String value3, String value4, String value5, String value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
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
    public ReportVersionDataRecord(Integer id, String reportVersion, String name, String database, String query, String hash) {
        super(ReportVersionData.REPORT_VERSION_DATA);

        set(0, id);
        set(1, reportVersion);
        set(2, name);
        set(3, database);
        set(4, query);
        set(5, hash);
    }
}
