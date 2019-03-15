/*
 * This file is generated by jOOQ.
*/
package org.vaccineimpact.orderlyweb.db.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionPackage;


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
public class ReportVersionPackageRecord extends UpdatableRecordImpl<ReportVersionPackageRecord> implements Record4<Integer, String, String, String> {

    private static final long serialVersionUID = -764242550;

    /**
     * Setter for <code>report_version_package.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>report_version_package.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>report_version_package.report_version</code>.
     */
    public void setReportVersion(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>report_version_package.report_version</code>.
     */
    public String getReportVersion() {
        return (String) get(1);
    }

    /**
     * Setter for <code>report_version_package.package_name</code>.
     */
    public void setPackageName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>report_version_package.package_name</code>.
     */
    public String getPackageName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>report_version_package.package_version</code>.
     */
    public void setPackageVersion(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>report_version_package.package_version</code>.
     */
    public String getPackageVersion() {
        return (String) get(3);
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
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return ReportVersionPackage.REPORT_VERSION_PACKAGE.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return ReportVersionPackage.REPORT_VERSION_PACKAGE.REPORT_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return ReportVersionPackage.REPORT_VERSION_PACKAGE.PACKAGE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return ReportVersionPackage.REPORT_VERSION_PACKAGE.PACKAGE_VERSION;
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
        return getPackageName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getPackageVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionPackageRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionPackageRecord value2(String value) {
        setReportVersion(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionPackageRecord value3(String value) {
        setPackageName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionPackageRecord value4(String value) {
        setPackageVersion(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionPackageRecord values(Integer value1, String value2, String value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ReportVersionPackageRecord
     */
    public ReportVersionPackageRecord() {
        super(ReportVersionPackage.REPORT_VERSION_PACKAGE);
    }

    /**
     * Create a detached, initialised ReportVersionPackageRecord
     */
    public ReportVersionPackageRecord(Integer id, String reportVersion, String packageName, String packageVersion) {
        super(ReportVersionPackage.REPORT_VERSION_PACKAGE);

        set(0, id);
        set(1, reportVersion);
        set(2, packageName);
        set(3, packageVersion);
    }
}
