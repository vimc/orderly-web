<#-- @ftlvariable name="reports"
 type="kotlin.collections.List<org.vaccineimpact.orderlyweb.models.ReportVersion>" -->
<@layout>
    <#macro styles>
        <link rel="stylesheet" type="text/css" href="/css/dataTables.min.css"/>
    </#macro>
    <table id="reports" style="width: 100%">
        <thead>
        <th>
            Name
        </th>
        <th>
            Version
        </th>
        <th>
            Author
        </th>
        <th>
            Requester
        </th>
        <th>
            Status
        </th>
        </thead>
        <tbody>
        <#list reports as report>
            <tr>
                <td>
                    ${report.name}
                </td>
                <td>
                    <a href="/reports/${report.name}/versions/${report.latestVersion}">${report.latestVersion}</a>
                </td>
                <td>
                    ${report.author}
                </td>
                <td>
                    ${report.requester}
                </td>
                <td>
                    <#include "partials/publishbadge.ftl">
                </td>
            </tr>
        </#list>
        </tbody>
    </table>

    <#macro scripts>
        <script src="/js/reports.bundle.js"></script>
    </#macro>
</@layout>