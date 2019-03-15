/*
 * This file is generated by jOOQ.
*/
package org.vaccineimpact.orderlyweb.db.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupReportPermission;


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
public class OrderlywebUserGroupReportPermissionRecord extends TableRecordImpl<OrderlywebUserGroupReportPermissionRecord> implements Record2<Integer, String> {

    private static final long serialVersionUID = 438360224;

    /**
     * Setter for <code>orderlyweb_user_group_report_permission.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>orderlyweb_user_group_report_permission.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>orderlyweb_user_group_report_permission.report</code>.
     */
    public void setReport(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>orderlyweb_user_group_report_permission.report</code>.
     */
    public String getReport() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<Integer, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<Integer, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return OrderlywebUserGroupReportPermission.ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return OrderlywebUserGroupReportPermission.ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.REPORT;
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
        return getReport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderlywebUserGroupReportPermissionRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderlywebUserGroupReportPermissionRecord value2(String value) {
        setReport(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderlywebUserGroupReportPermissionRecord values(Integer value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OrderlywebUserGroupReportPermissionRecord
     */
    public OrderlywebUserGroupReportPermissionRecord() {
        super(OrderlywebUserGroupReportPermission.ORDERLYWEB_USER_GROUP_REPORT_PERMISSION);
    }

    /**
     * Create a detached, initialised OrderlywebUserGroupReportPermissionRecord
     */
    public OrderlywebUserGroupReportPermissionRecord(Integer id, String report) {
        super(OrderlywebUserGroupReportPermission.ORDERLYWEB_USER_GROUP_REPORT_PERMISSION);

        set(0, id);
        set(1, report);
    }
}
