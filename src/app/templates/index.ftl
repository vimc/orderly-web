<#-- @ftlvariable name="reportsJson" type="String" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<#-- @ftlvariable name="isReportRunner" type="Boolean" -->
<#-- @ftlvariable name="showProjectDocs" type="Boolean" -->
<#macro if if then else=""><#if if>${then}<#else>${else}</#if></#macro>
<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/index.min.css"/>
    </#macro>
    <#include "partials/pinned-reports.ftl">
    <#if showProjectDocs>
        <h3 class="mb-3">
            <button class="btn-link btn btn-lg"><a href="${appUrl}/project-docs">View project documentation
                <svg height="40" class="octicon octicon-arrow-right"
                     viewBox="0 0 10 16"
                     version="1.1"
                     width="30"
                     aria-hidden="true"
                     fill="currentColor">
                    <path fill-rule="evenodd" d="M10 8L4 3v3H0v4h4v3l6-5z"></path>
                </svg>
                </a>
            </button>
        </h3>
    </#if>
    <#if isReportRunner>
        <h3 class="mb-3">
            <button class="ml-5 btn-link btn btn-lg"><a href="${appUrl}/run-report">Run a report
                    <svg height="40" class="octicon octicon-arrow-right"
                         viewBox="0 0 10 16"
                         version="1.1"
                         width="30"
                         aria-hidden="true"
                         fill="currentColor">
                        <path fill-rule="evenodd" d="M10 8L4 3v3H0v4h4v3l6-5z"></path>
                    </svg>
                </a>
            </button>
        </h3>
    </#if>
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

            <th>
                <label for="tags-filter">Tags</label>
            </th>
            <th>
                <label for="parameter-values-filter">Parameter Values</label>
            </th>

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
            <th>
                <select class="form-control" id="tags-filter"
                        multiple="multiple"
                        <#if isReviewer>
                            data-col="4"
                        <#else>
                            data-col="3"
                        </#if>>
                    <#list tags as tag>
                        <option value="${tag}">${tag}</option>
                    </#list>
                </select>
            </th>
            <th>
                <input class="form-control" type="text" id="parameter-values-filter"
                       placeholder="Type to filter..."
                       data-role="standard-filter"
                        <#if isReviewer>
                            data-col="5"
                        <#else>
                            data-col="4"
                        </#if>/>
            </th>
            <#list customFieldKeys as customField>
                <th>
                    <input class="form-control" type="text" id="${customField}-filter"
                           placeholder="Type to filter..."
                           data-role="standard-filter"
                            <#if isReviewer>
                                data-col="${customField?index + 6}"
                            <#else>
                                data-col="${customField?index + 5}"
                            </#if>
                    />
                </th>
            </#list>
        </tr>
        </thead>
    </table>
    <#macro scripts>
        <script type="text/javascript">
            var customFields = [<#list customFieldKeys as customField>"${customField}", </#list>];
            var rawReports = ${reportsJson};
            var reports = rawReports.map(r => ({...r, ...r.custom_fields}));
            <#if isReviewer>
            var canReview = true;
            </#if>
            <#if canSetPinnedReports>
            var reportDisplayNames = ${reportDisplayNamesJson};
            var currentPinnedReportNames = [
                <#list pinnedReports as pinnedReport>
                "${pinnedReport.name}",
                </#list>];
            </#if>
        </script>
        <script type="text/javascript" src="${appUrl}/js/lib/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="${appUrl}/js/lib/dataTables.bootstrap4.js"></script>
        <script type="text/javascript" src="${appUrl}/js/lib/dataTables.dataTables.js"></script>
        <script type="text/javascript" src="${appUrl}/js/index.bundle.js"></script>
    </#macro>
</@layout>