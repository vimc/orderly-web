<#-- @ftlvariable name="reports"
 type="kotlin.collections.List<org.vaccineimpact.orderlyweb.models.Report>" -->
<html>
<head></head>
<body>
<h1>All reports</h1>
<ul>
    <#list reports as report>
        <li>${report.name}</li>
    </#list>
</ul>
</body>
</html>