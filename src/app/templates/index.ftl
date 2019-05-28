<#-- @ftlvariable name="reportsJson" type="String" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<@layout>
    <#macro styles>
        <link rel="stylesheet" href="/css/index.min.css"/>
    </#macro>
    <h1 class="h3 mb-3">Find a report</h1>
<div class="helper-text text-muted mb-2">Click on a column heading to sort by that field. Hold shift to multi-sort.</div>
    <table id="reports-table" class="table table-striped">
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
        <script type="text/javascript">
            var reports = ${reportsJson}
           <#if isReviewer>
                 var isReviewer = true;
           </#if>
        </script>
    <script type="text/javascript" src="js/index.bundle.js"></script>
    </#macro>
</@layout>