/*
 * This file is generated by jOOQ.
*/
package org.vaccineimpact.reporting_api.db.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;
import org.vaccineimpact.reporting_api.db.tables.ReportVersionArtefact;


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
public class ReportVersionArtefactRecord extends UpdatableRecordImpl<ReportVersionArtefactRecord> implements Record5<Integer, String, String, String, Integer> {

    private static final long serialVersionUID = 1050266802;

    /**
     * Setter for <code>report_version_artefact.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>report_version_artefact.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>report_version_artefact.report_version</code>.
     */
    public void setReportVersion(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>report_version_artefact.report_version</code>.
     */
    public String getReportVersion() {
        return (String) get(1);
    }

    /**
     * Setter for <code>report_version_artefact.format</code>.
     */
    public void setFormat(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>report_version_artefact.format</code>.
     */
    public String getFormat() {
        return (String) get(2);
    }

    /**
     * Setter for <code>report_version_artefact.description</code>.
     */
    public void setDescription(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>report_version_artefact.description</code>.
     */
    public String getDescription() {
        return (String) get(3);
    }

    /**
     * Setter for <code>report_version_artefact.order</code>.
     */
    public void setOrder(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>report_version_artefact.order</code>.
     */
    public Integer getOrder() {
        return (Integer) get(4);
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
    public Row5<Integer, String, String, String, Integer> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Integer, String, String, String, Integer> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return ReportVersionArtefact.REPORT_VERSION_ARTEFACT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return ReportVersionArtefact.REPORT_VERSION_ARTEFACT.REPORT_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return ReportVersionArtefact.REPORT_VERSION_ARTEFACT.FORMAT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return ReportVersionArtefact.REPORT_VERSION_ARTEFACT.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field5() {
        return ReportVersionArtefact.REPORT_VERSION_ARTEFACT.ORDER;
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
        return getFormat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value5() {
        return getOrder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionArtefactRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionArtefactRecord value2(String value) {
        setReportVersion(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionArtefactRecord value3(String value) {
        setFormat(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionArtefactRecord value4(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionArtefactRecord value5(Integer value) {
        setOrder(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportVersionArtefactRecord values(Integer value1, String value2, String value3, String value4, Integer value5) {
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
     * Create a detached ReportVersionArtefactRecord
     */
    public ReportVersionArtefactRecord() {
        super(ReportVersionArtefact.REPORT_VERSION_ARTEFACT);
    }

    /**
     * Create a detached, initialised ReportVersionArtefactRecord
     */
    public ReportVersionArtefactRecord(Integer id, String reportVersion, String format, String description, Integer order) {
        super(ReportVersionArtefact.REPORT_VERSION_ARTEFACT);

        set(0, id);
        set(1, reportVersion);
        set(2, format);
        set(3, description);
        set(4, order);
    }
}
