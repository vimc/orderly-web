<#-- @ftlvariable name="report"
 type="org.vaccineimpact.orderlyweb.models.ReportVersion" -->
<span class="${report.published?then("badge-published", "badge-internal")} badge float-left">
    ${report.published?then("published", "internal")} </span>