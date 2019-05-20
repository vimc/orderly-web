<#-- @ftlvariable name="versions" type="kotlin.collections.List<org.vaccineimpact.orderlyweb.models.VersionPickerViewModel>" -->
<div>
    <label for="report-version-switcher"
           class="mt-0 font-weight-bold"> Version
    </label>
    <select class="form-control form-control-sm" id="report-version-switcher"
            onChange="window.location=this.value">
        <#list versions as version>
            <#if version.selected>
                <option value="${version.url}" selected>${version.date}</option>
            <#else>
                <option value="${version.url}">${version.date}</option>
            </#if>
        </#list>
    </select>
</div>
