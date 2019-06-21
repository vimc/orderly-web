<#-- @ftlvariable name="reportsJson" type="String" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/index.min.css"/>
    </#macro>
    <#include "partials/pinned-reports.ftl">
<h1 class="h3 mb-3 reports-list">Find a report</h1>
<div class="helper-text text-muted mb-2">Click on a column heading to sort by that field. Hold shift to multi-sort.
</div>
<div role="group" class="mb-3 btn-group">
    <a href="#" class="mr-2" id="collapse">Collapse all reports</a>/<a href="#" class="ml-2" id="expand">Expand all
    reports</a>
</div>
    <table id="reports-table" class="table display table-striped" style="width:100%">
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
                 var canReview = true;
           </#if>
        </script>

    <script type="text/javascript" src="${appUrl}/js/index.bundle.js"></script>
    </#macro>
</@layout>