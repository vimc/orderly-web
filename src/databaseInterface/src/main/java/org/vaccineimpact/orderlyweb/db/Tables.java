/*
 * This file is generated by jOOQ.
 */
package org.vaccineimpact.orderlyweb.db;


import org.vaccineimpact.orderlyweb.db.tables.ArtefactFormat;
import org.vaccineimpact.orderlyweb.db.tables.Changelog;
import org.vaccineimpact.orderlyweb.db.tables.ChangelogLabel;
import org.vaccineimpact.orderlyweb.db.tables.CustomFields;
import org.vaccineimpact.orderlyweb.db.tables.Data;
import org.vaccineimpact.orderlyweb.db.tables.Depends;
import org.vaccineimpact.orderlyweb.db.tables.File;
import org.vaccineimpact.orderlyweb.db.tables.FileArtefact;
import org.vaccineimpact.orderlyweb.db.tables.FileInput;
import org.vaccineimpact.orderlyweb.db.tables.FileInputGlobal;
import org.vaccineimpact.orderlyweb.db.tables.FilePurpose;
import org.vaccineimpact.orderlyweb.db.tables.OrderlySchema;
import org.vaccineimpact.orderlyweb.db.tables.OrderlySchemaTables;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebDocument;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebPermission;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebPinnedReportGlobal;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebReportRun;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebReportTag;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebReportVersion;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebReportVersionFull;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebReportVersionTag;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebSettings;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUser;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroup;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupGlobalPermission;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupPermission;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupPermissionAll;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupReportPermission;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupUser;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebUserGroupVersionPermission;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebWorkflowRun;
import org.vaccineimpact.orderlyweb.db.tables.OrderlywebWorkflowRunReports;
import org.vaccineimpact.orderlyweb.db.tables.Parameters;
import org.vaccineimpact.orderlyweb.db.tables.ParametersType;
import org.vaccineimpact.orderlyweb.db.tables.Report;
import org.vaccineimpact.orderlyweb.db.tables.ReportBatch;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersion;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionArtefact;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionBatch;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionCustomFields;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionData;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionInstance;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionPackage;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionTag;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionView;
import org.vaccineimpact.orderlyweb.db.tables.ReportVersionWorkflow;
import org.vaccineimpact.orderlyweb.db.tables.Tag;
import org.vaccineimpact.orderlyweb.db.tables.Workflow;


/**
 * Convenience access to all tables in 
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>artefact_format</code>.
     */
    public static final ArtefactFormat ARTEFACT_FORMAT = ArtefactFormat.ARTEFACT_FORMAT;

    /**
     * The table <code>changelog</code>.
     */
    public static final Changelog CHANGELOG = Changelog.CHANGELOG;

    /**
     * The table <code>changelog_label</code>.
     */
    public static final ChangelogLabel CHANGELOG_LABEL = ChangelogLabel.CHANGELOG_LABEL;

    /**
     * The table <code>custom_fields</code>.
     */
    public static final CustomFields CUSTOM_FIELDS = CustomFields.CUSTOM_FIELDS;

    /**
     * The table <code>data</code>.
     */
    public static final Data DATA = Data.DATA;

    /**
     * The table <code>depends</code>.
     */
    public static final Depends DEPENDS = Depends.DEPENDS;

    /**
     * The table <code>file</code>.
     */
    public static final File FILE = File.FILE;

    /**
     * The table <code>file_artefact</code>.
     */
    public static final FileArtefact FILE_ARTEFACT = FileArtefact.FILE_ARTEFACT;

    /**
     * The table <code>file_input</code>.
     */
    public static final FileInput FILE_INPUT = FileInput.FILE_INPUT;

    /**
     * The table <code>file_input_global</code>.
     */
    public static final FileInputGlobal FILE_INPUT_GLOBAL = FileInputGlobal.FILE_INPUT_GLOBAL;

    /**
     * The table <code>file_purpose</code>.
     */
    public static final FilePurpose FILE_PURPOSE = FilePurpose.FILE_PURPOSE;

    /**
     * The table <code>orderly_schema</code>.
     */
    public static final OrderlySchema ORDERLY_SCHEMA = OrderlySchema.ORDERLY_SCHEMA;

    /**
     * The table <code>orderly_schema_tables</code>.
     */
    public static final OrderlySchemaTables ORDERLY_SCHEMA_TABLES = OrderlySchemaTables.ORDERLY_SCHEMA_TABLES;

    /**
     * The table <code>orderlyweb_document</code>.
     */
    public static final OrderlywebDocument ORDERLYWEB_DOCUMENT = OrderlywebDocument.ORDERLYWEB_DOCUMENT;

    /**
     * The table <code>orderlyweb_permission</code>.
     */
    public static final OrderlywebPermission ORDERLYWEB_PERMISSION = OrderlywebPermission.ORDERLYWEB_PERMISSION;

    /**
     * The table <code>orderlyweb_pinned_report_global</code>.
     */
    public static final OrderlywebPinnedReportGlobal ORDERLYWEB_PINNED_REPORT_GLOBAL = OrderlywebPinnedReportGlobal.ORDERLYWEB_PINNED_REPORT_GLOBAL;

    /**
     * The table <code>orderlyweb_report_run</code>.
     */
    public static final OrderlywebReportRun ORDERLYWEB_REPORT_RUN = OrderlywebReportRun.ORDERLYWEB_REPORT_RUN;

    /**
     * The table <code>orderlyweb_report_tag</code>.
     */
    public static final OrderlywebReportTag ORDERLYWEB_REPORT_TAG = OrderlywebReportTag.ORDERLYWEB_REPORT_TAG;

    /**
     * The table <code>orderlyweb_report_version</code>.
     */
    public static final OrderlywebReportVersion ORDERLYWEB_REPORT_VERSION = OrderlywebReportVersion.ORDERLYWEB_REPORT_VERSION;

    /**
     * The table <code>orderlyweb_report_version_full</code>.
     */
    public static final OrderlywebReportVersionFull ORDERLYWEB_REPORT_VERSION_FULL = OrderlywebReportVersionFull.ORDERLYWEB_REPORT_VERSION_FULL;

    /**
     * The table <code>orderlyweb_report_version_tag</code>.
     */
    public static final OrderlywebReportVersionTag ORDERLYWEB_REPORT_VERSION_TAG = OrderlywebReportVersionTag.ORDERLYWEB_REPORT_VERSION_TAG;

    /**
     * The table <code>orderlyweb_settings</code>.
     */
    public static final OrderlywebSettings ORDERLYWEB_SETTINGS = OrderlywebSettings.ORDERLYWEB_SETTINGS;

    /**
     * The table <code>orderlyweb_user</code>.
     */
    public static final OrderlywebUser ORDERLYWEB_USER = OrderlywebUser.ORDERLYWEB_USER;

    /**
     * The table <code>orderlyweb_user_group</code>.
     */
    public static final OrderlywebUserGroup ORDERLYWEB_USER_GROUP = OrderlywebUserGroup.ORDERLYWEB_USER_GROUP;

    /**
     * The table <code>orderlyweb_user_group_global_permission</code>.
     */
    public static final OrderlywebUserGroupGlobalPermission ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION = OrderlywebUserGroupGlobalPermission.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION;

    /**
     * The table <code>orderlyweb_user_group_permission</code>.
     */
    public static final OrderlywebUserGroupPermission ORDERLYWEB_USER_GROUP_PERMISSION = OrderlywebUserGroupPermission.ORDERLYWEB_USER_GROUP_PERMISSION;

    /**
     * The table <code>orderlyweb_user_group_permission_all</code>.
     */
    public static final OrderlywebUserGroupPermissionAll ORDERLYWEB_USER_GROUP_PERMISSION_ALL = OrderlywebUserGroupPermissionAll.ORDERLYWEB_USER_GROUP_PERMISSION_ALL;

    /**
     * The table <code>orderlyweb_user_group_report_permission</code>.
     */
    public static final OrderlywebUserGroupReportPermission ORDERLYWEB_USER_GROUP_REPORT_PERMISSION = OrderlywebUserGroupReportPermission.ORDERLYWEB_USER_GROUP_REPORT_PERMISSION;

    /**
     * The table <code>orderlyweb_user_group_user</code>.
     */
    public static final OrderlywebUserGroupUser ORDERLYWEB_USER_GROUP_USER = OrderlywebUserGroupUser.ORDERLYWEB_USER_GROUP_USER;

    /**
     * The table <code>orderlyweb_user_group_version_permission</code>.
     */
    public static final OrderlywebUserGroupVersionPermission ORDERLYWEB_USER_GROUP_VERSION_PERMISSION = OrderlywebUserGroupVersionPermission.ORDERLYWEB_USER_GROUP_VERSION_PERMISSION;

    /**
     * The table <code>orderlyweb_workflow_run</code>.
     */
    public static final OrderlywebWorkflowRun ORDERLYWEB_WORKFLOW_RUN = OrderlywebWorkflowRun.ORDERLYWEB_WORKFLOW_RUN;

    /**
     * The table <code>orderlyweb_workflow_run_reports</code>.
     */
    public static final OrderlywebWorkflowRunReports ORDERLYWEB_WORKFLOW_RUN_REPORTS = OrderlywebWorkflowRunReports.ORDERLYWEB_WORKFLOW_RUN_REPORTS;

    /**
     * The table <code>parameters</code>.
     */
    public static final Parameters PARAMETERS = Parameters.PARAMETERS;

    /**
     * The table <code>parameters_type</code>.
     */
    public static final ParametersType PARAMETERS_TYPE = ParametersType.PARAMETERS_TYPE;

    /**
     * The table <code>report</code>.
     */
    public static final Report REPORT = Report.REPORT;

    /**
     * The table <code>report_batch</code>.
     */
    public static final ReportBatch REPORT_BATCH = ReportBatch.REPORT_BATCH;

    /**
     * The table <code>report_version</code>.
     */
    public static final ReportVersion REPORT_VERSION = ReportVersion.REPORT_VERSION;

    /**
     * The table <code>report_version_artefact</code>.
     */
    public static final ReportVersionArtefact REPORT_VERSION_ARTEFACT = ReportVersionArtefact.REPORT_VERSION_ARTEFACT;

    /**
     * The table <code>report_version_batch</code>.
     */
    public static final ReportVersionBatch REPORT_VERSION_BATCH = ReportVersionBatch.REPORT_VERSION_BATCH;

    /**
     * The table <code>report_version_custom_fields</code>.
     */
    public static final ReportVersionCustomFields REPORT_VERSION_CUSTOM_FIELDS = ReportVersionCustomFields.REPORT_VERSION_CUSTOM_FIELDS;

    /**
     * The table <code>report_version_data</code>.
     */
    public static final ReportVersionData REPORT_VERSION_DATA = ReportVersionData.REPORT_VERSION_DATA;

    /**
     * The table <code>report_version_instance</code>.
     */
    public static final ReportVersionInstance REPORT_VERSION_INSTANCE = ReportVersionInstance.REPORT_VERSION_INSTANCE;

    /**
     * The table <code>report_version_package</code>.
     */
    public static final ReportVersionPackage REPORT_VERSION_PACKAGE = ReportVersionPackage.REPORT_VERSION_PACKAGE;

    /**
     * The table <code>report_version_tag</code>.
     */
    public static final ReportVersionTag REPORT_VERSION_TAG = ReportVersionTag.REPORT_VERSION_TAG;

    /**
     * The table <code>report_version_view</code>.
     */
    public static final ReportVersionView REPORT_VERSION_VIEW = ReportVersionView.REPORT_VERSION_VIEW;

    /**
     * The table <code>report_version_workflow</code>.
     */
    public static final ReportVersionWorkflow REPORT_VERSION_WORKFLOW = ReportVersionWorkflow.REPORT_VERSION_WORKFLOW;

    /**
     * The table <code>tag</code>.
     */
    public static final Tag TAG = Tag.TAG;

    /**
     * The table <code>workflow</code>.
     */
    public static final Workflow WORKFLOW = Workflow.WORKFLOW;
}
