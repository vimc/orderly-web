package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.repositories.ArtefactRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyArtefactRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.BasicReportVersion
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails

interface ReportLogic
{
    @Throws(UnknownObjectError::class)
    fun getDetailsByNameAndVersion(name: String, version: String): ReportVersionDetails
    fun getAllReportVersions(): List<ReportVersion>
}

class OrderlyReportLogic(private val reportRepository: ReportRepository,
                         private val artefactRepository: ArtefactRepository,
                         private val orderly: OrderlyClient) : ReportLogic
{

    constructor(context: ActionContext) : this(OrderlyReportRepository(context), OrderlyArtefactRepository(), Orderly(context))

    override fun getDetailsByNameAndVersion(name: String, version: String): ReportVersionDetails
    {
        val basicReportVersion = reportRepository.getReportVersion(name, version)

        return ReportVersionDetails(basicReportVersion,
                artefacts = artefactRepository.getArtefacts(name, version),
                resources = orderly.getResourceFiles(name, version),
                dataInfo = orderly.getDataInfo(name, version),
                parameterValues = orderly.getParameters(version))

    }

    override fun getAllReportVersions(): List<ReportVersion>
    {
        val basicVersions = reportRepository.getAllReportVersions()
        return mapToReportVersions(basicVersions)
    }

    private fun mapToReportVersions(basicVersions: List<BasicReportVersion>): List<ReportVersion>
    {
        val allCustomFields = reportRepository.getCustomFields()
        val versionIds = basicVersions.map { it.id }
        val customFieldsForVersions = reportRepository.getCustomFieldsForVersions(versionIds)

        val parametersForVersions = orderly.getParametersForVersions(versionIds)

        val allVersionTags = orderly.getVersionTags(versionIds)
        val allReportTags = orderly.getReportTagsForVersions(versionIds)
        val allOrderlyTags = orderly.getOrderlyTags(versionIds)

        return basicVersions.map {
            val versionId = it.id
            val versionCustomFields = mutableMapOf<String, String?>()

            versionCustomFields.putAll(allCustomFields)
            versionCustomFields.putAll(customFieldsForVersions[versionId]?: listOf())

            val versionParameters = parametersForVersions[versionId] ?: mapOf()

            val versionTags = allVersionTags[versionId] ?: listOf()
            val reportTags = allReportTags[versionId] ?: listOf()
            val orderlyTags = allOrderlyTags[versionId] ?: listOf()

            ReportVersion(it,
                    versionCustomFields,
                    versionParameters,
                    (versionTags + reportTags + orderlyTags).distinct().sorted())
        }

    }

}