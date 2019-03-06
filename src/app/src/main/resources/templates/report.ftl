<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.Report" -->
<#-- @ftlvariable name="reportDetailsJson" type="String" -->
<@layout>
    <#macro styles>
    <link rel="stylesheet" href="/css/report-page.min.css"/>
    </#macro>
<h1>${report.name}</h1>
<div class="row" id="vueApp">
    <div class="col-2">
        <div v-on:click="publish"
             v-bind:class="[{'toggle':true}, 'btn', {'btn-primary':published}, {'off':!published}]" data-toggle="toggle"
             style="width: 109.281px; height: 38px;">
            <div class="toggle-group">
                <label class="btn btn-primary toggle-on">Published</label>
                <label class="btn btn-default active toggle-off">Internal</label>
                <span class="toggle-handle btn btn-default">
            </span>
            </div>
        </div>
    </div>
    <div class="col-10">
        <h2>Details</h2>
    </div>
</div>
    <#macro scripts>
<script>
    var report = ${reportDetailsJson}
</script>
    <script src="/js/report.bundle.js"></script>
    </#macro>
</@layout>
