<#-- @ftlvariable name="reportsJson" type="String" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<#macro if if then else=""><#if if>${then}<#else>${else}</#if></#macro>
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
    <table id="reports-table" class="table display table-striped" style="width:100%; table-layout: fixed">
        <thead>
        <tr>
            <th>
                <label for="name-filter">Name</label>

            </th>
            <th>
                <label for="version-filter">Version</label>

            </th>
            <#if isReviewer>
                <th style="width:100px"><label for="status-filter">Status</label>
                </th>
            </#if>

            <#list customFieldKeys as customField>
                <th>
                    <label for="${customField}-filter">${customField?cap_first}</label>

                </th>
            </#list>
        </tr>
        <tr>
            <th>
                <input class="form-control" type="text" id="name-filter" placeholder="Type to filter..."/>
            </th>
            <th>

                <input class="form-control" type="text" id="version-filter"
                       data-role="standard-filter" placeholder="Type to filter..."
                       data-col="2"/>
            </th>
            <#if isReviewer>
                <th>
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
            <#list customFieldKeys as customField>
                <th>
                    <input class="form-control" type="text" id="${customField}-filter"
                           placeholder="Type to filter..."
                           data-role="standard-filter"
                           <#if isReviewer>
                               data-col="${customField?index + 4}"
                           <#else>
                               data-col="${customField?index + 3}"
                           </#if>
                    />
                </th>
            </#list>
        </tr>
        </thead>
    </table>
    <#macro scripts>
        <script type="text/javascript">
            var customFields = [<#list customFieldKeys as customField>"${customField}",</#list>];
            var reports = reports.map(r => ({...r, ...r.customFields}));
            <#if isReviewer>
            var canReview = true;
            </#if>
        </script>
    <script type="text/javascript" src="${appUrl}/js/index.bundle.js"></script>
    </#macro>
</@layout>