<#-- @ftlvariable name="changelog" type="kotlin.collections.List<org.vaccineimpact.orderlyweb.viewmodels.ChangelogViewModel>" -->
<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->

<h3 class="mb3">Changelog</h3>

<#if changelog?size == 0 >
<p>There is no changelog for this report version</p>
<#else>
<table class="table-responsive table-bordered table border-0">
    <tbody>
    <#list changelog as item>
    <tr>
        <td class="changelog-date">
            <a href="/reports/${report.name}/${item.version}">${item.date}</a>
        </td>
        <td>
            <#list item.entries as entry>
                <div class="badge changelog-label badge-${entry.label}">${entry.label}</div>
                <div class="changelog-item ${entry.label}">
                    ${entry.value}
                </div>
            </#list>
        </td>
    </tr>
    </#list>
    </tbody>
</table>
</#if>