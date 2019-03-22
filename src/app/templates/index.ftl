<#-- @ftlvariable name="reports"
type="kotlin.collections.List<org.vaccineimpact.orderlyweb.models.Report>" -->
<@layout>
    <#list reports as report>
        <span>${report.name}</span>
    </#list>
</@layout>