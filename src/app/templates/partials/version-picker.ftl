<#-- @ftlvariable name="versions" type="kotlin.collections.List<String>" -->
<div>
    <label
            for="report-version-switcher"
            class="mt-0 font-weight-bold">
        Version
    </label>
    <select class="form-control form-control-sm" id="report-version-switcher">
         <#list versions as version>

         </#list>
    </select>
</div>