<#-- @ftlvariable name="reportsJson" type="kotlin.collections.List<org.vaccineimpact.orderlyweb.models.Report>" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<@layout>
    <h1>Find a report</h1>
    <table id="reports-table">
        <thead>
        <tr>
            <th>Name</th>
            <th>Id</th>
            <#if isReviewer>
                <th>Status</th>
            </#if>
            <th>Author</th>
            <th>Requester</th>
        </tr>
        </thead>
    </table>
    <#macro scripts>
        <script>
            var report = ${reportsJson}
        </script>
    </#macro>
</@layout>