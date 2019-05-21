<#-- @ftlvariable name="reportJson" type="String" -->
<@layout>
    <#macro styles>
        <link rel="stylesheet" href="/css/index.min.css"/>
    </#macro>
    <h1>Find a report</h1>
<table id="reports-table" style="width: 100%" class="stripe responsive">
    <thead>
    <tr>
        <th></th>
        <th>Name</th>
        <th>Id</th>
        <th>Status</th>
        <th>Author</th>
        <th>Requester</th></tr>
    </thead>
</table>
</@layout>
<#macro scripts>
        <script>
            var reports = ${reportsJson}
        </script>
<script src="/js/index.bundle.js"></script>
</#macro>