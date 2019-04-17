<#-- @ftlvariable name="reports" type="kotlin.collections.List<org.vaccineimpact.orderlyweb.models.Report>" -->
<@layout>
    <h1>All reports</h1>
    <#list reports as report>
        <a href="reports/${report.name}/${report.latestVersion}">${report.name}</a>
    </#list>
</@layout>