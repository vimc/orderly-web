<#-- @ftlvariable name="reportsJson" type="String" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<#macro if if then else=""><#if if>${then}<#else>${else}</#if></#macro>
<@layout>
    <#macro styles>
        <link rel="stylesheet" href="/css/index.min.css"/>
    </#macro>
    <#include "partials/pinned-reports.ftl">
<h1 class="h3 mb-3 reports-list">Find a report</h1>
<div class="helper-text text-muted mb-2">Click on a column heading to sort by that field. Hold shift to multi-sort.
</div>
<div role="group" class="mb-3 btn-group">
    <a href="#" class="mr-2" id="collapse">Collapse all reports</a>/<a href="#" class="ml-2" id="expand">Expand all
    reports</a>
</div>
<table id="reports-table" class="table dt-responsive display table-striped">
    <thead>
    <tr>
        <th>
            <label for="name-filter">Name</label>
            <input class="form-control" type="text" id="name-filter"
                   data-role="standard-filter"
                   data-col="1"/>
        </th>
        <th>
            <label for="version-filter">Version</label>
            <input class="form-control" type="text" id="version-filter"
                   data-role="standard-filter"
                   data-col="2"/>
        </th>
            <#if isReviewer>
                <th><label for="status-filter">Status</label>
                    <select id="status-filter" class="form-control">
                        <option value="all">
                            All
                        </option>
                        <option value="published">
                            Published
                        </option>
                        <option value="internal">
                            Internal
                        </option>
                    </select>
                </th>
            </#if>
        <th>
            <label for="author-filter">Author</label>
            <input class="form-control" type="text" id="author-filter"
                   data-role="standard-filter"
                   data-col="<@if isReviewer "4" "3"/>"/>
        </th>
        <th>
            <label for="requester-filter">Requester</label>
            <input class="form-control" type="text" id="requester-filter"
                   data-role="standard-filter"
                   data-col="<@if isReviewer "5" "4"/>"/>
        </th>
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
    <script type="text/javascript" src="/js/index.bundle.js"></script>
    </#macro>
</@layout>