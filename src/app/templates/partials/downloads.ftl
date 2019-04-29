<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetails" -->
<#-- @ftlvariable name="artefacts" type="List<org.vaccineimpact.orderlyweb.controllers.web.ArtefactViewModel>" -->
<h2>Downloads</h2>

<#list artefacts as artefact>
    <div class="mb-2 card">
        <div class="card-header">${artefact.artefact.description}</div>
        <div class="card-body">
            <#if artefact.inlineArtefactFigure??>
                <img src="${artefact.inlineArtefactFigure}" class="border border-dark p-3 col-12 col-lg-8">
            </#if>

            <!-- links -->
            <#list artefact.files as file>
                <div><a target="_blank" href="${file.url}">${file.fileName} <#include "download-icon.ftl"></a></div>
            </#list>
        </div>
    </div>
</#list>