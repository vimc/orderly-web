<#-- @ftlvariable name="reports"
type="kotlin.collections.List<org.vaccineimpact.orderlyweb.models.Report>" -->
<@layout>
    <h1>All reports</h1>
    <#list reports as report>
        <span>${report.name}</span>
    </#list>
</@layout>