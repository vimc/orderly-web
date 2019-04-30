<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetails" -->
<#-- @ftlvariable name="artefacts" type="List<org.vaccineimpact.orderlyweb.controllers.web.ArtefactViewModel>" -->
<#-- @ftlvariable name="dataLinks" type="List<org.vaccineimpact.orderlyweb.controllers.web.InputDataModel>" -->
<#-- @ftlvariable name="resources" type="List<org.vaccineimpact.orderlyweb.controllers.web.DownloadableFileViewModel>" -->
<#-- @ftlvariable name="zipFile" type="org.vaccineimpact.orderlyweb.controllers.web.DownloadableFileViewModel" -->

<#import "../macros/file-download-link.ftl" as file_download_link>

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
                <div><@file_download_link.render file=file /></div>
            </#list>
        </div>
    </div>
</#list>

<#if dataLinks?has_content>
     <div class="mb-2 card">
        <div class="card-header">Input data to the report</div>
        <div class="card-body">
            <#list dataLinks as dataLink>
                <div class="row">
                    <div class="col-12 col-md-3">${dataLink.key}</div>
                    <div class="col-12 col-md-9">
                        <ul>
                            <li><@file_download_link.render file=dataLink.csv /></li>
                            <li><@file_download_link.render file=dataLink.rds /></li>
                        </ul>
                    </div>
                </div>
            </#list>
        </div>
    </div>
</#if>

<#if resources?has_content>
    <div class="mb-2 card">
      <div class="card-header">Resources</div>
      <div class="card-body">
        <#list resources as resource>
            <div><@file_download_link.render file=resource /></div>
        </#list>
      </div>
    </div>
</#if>

<div class="mb-5 mt-5">
    <div><@file_download_link.render file=zipFile class="button" iconColor="#ffffff" /></div>
</div>