package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.repositories.*
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*

class Orderly(val isReviewer: Boolean,
              val isGlobalReader: Boolean,
              val reportReadingScopes: List<String> = listOf(),
              val reportRepository: ReportRepository = OrderlyReportRepository(isReviewer, isGlobalReader, reportReadingScopes),
              val artefactRepository: ArtefactRepository = OrderlyArtefactRepository(),
              val tagRepository: TagRepository = OrderlyWebTagRepository()) : OrderlyClient
{
    constructor(context: ActionContext) : this(context.isReviewer(), context.isGlobalReader(), context.reportReadingScopes)

    override fun getAllReportVersions(): List<ReportVersionWithDescCustomFieldsLatestParamsTags>
    {
        val basicVersions = reportRepository.getAllReportVersions()
        return mapToReportVersions(basicVersions)
    }

    override fun getDetailsByNameAndVersion(name: String, version: String): ReportVersionWithArtefactsDataDescParamsResources
    {
        val basicReportVersion = reportRepository.getReportVersion(name, version)
        val artefacts = artefactRepository.getArtefacts(name, version)
        val parameterValues = reportRepository.getParametersForVersions(listOf(version))[version] ?: mapOf()
        val instances = reportRepository.getReportVersionInstance(version)

        return ReportVersionWithArtefactsDataDescParamsResources(basicReportVersion,
                artefacts = artefacts,
                resources = getResourceFiles(name, version),
                dataInfo = getDataInfo(name, version),
                parameterValues = parameterValues,
                instances = instances)
    }

    override fun getReportVersionTags(name: String, version: String): ReportVersionTags
    {
        reportRepository.getReportVersion(name, version)

        val versionTags = tagRepository.getVersionTags(listOf(version))[version] ?: listOf()
        val reportTags = tagRepository.getReportTagsForVersions(listOf(version))[version] ?: listOf()
        val orderlyTags = tagRepository.getOrderlyTagsForVersions(listOf(version))[version] ?: listOf()

        return ReportVersionTags(versionTags.sorted(), reportTags.sorted(), orderlyTags.sorted())
    }

    override fun getData(name: String, version: String): Map<String, String>
    {
        reportRepository.getReportVersion(name, version)

        JooqContext().use {
            return it.dsl.select(
                    REPORT_VERSION_DATA.NAME,
                    REPORT_VERSION_DATA.HASH)
                    .from(REPORT_VERSION_DATA)
                    .where(REPORT_VERSION_DATA.REPORT_VERSION.eq(version))
                    .fetch()
                    .associate { it[REPORT_VERSION_DATA.NAME] to it[REPORT_VERSION_DATA.HASH] }
        }
    }

    override fun getDatum(name: String, version: String, datumname: String): String
    {
        val data = getData(name, version)
        return data[datumname] ?: throw UnknownObjectError(datumname, "Data")
    }

    override fun getResourceHashes(name: String, version: String): Map<String, String>
    {
        reportRepository.getReportVersion(name, version)

        return JooqContext().use { ctx ->
            ctx.dsl.select(FILE_INPUT.FILENAME, FILE_INPUT.FILE_HASH)
                    .from(FILE_INPUT)
                    .where(FILE_INPUT.REPORT_VERSION.eq(version))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.RESOURCE.toString()))
                    .fetch()
                    .associate { it[FILE_INPUT.FILENAME] to it[FILE_INPUT.FILE_HASH] }
        }
    }

    override fun getResourceHash(name: String, version: String, resourcename: String): String
    {
        return getResourceHashes(name, version)[resourcename]
                ?: throw UnknownObjectError(resourcename, "Resource")
    }

    override fun getReadme(name: String, version: String): Map<String, String>
    {
        reportRepository.getReportVersion(name, version)

        return JooqContext().use { ctx ->
            ctx.dsl.select(FILE_INPUT.FILENAME, FILE_INPUT.FILE_HASH)
                    .from(FILE_INPUT)
                    .where(FILE_INPUT.REPORT_VERSION.eq(version))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.README.toString()))
                    .fetch()
                    .associate { it[FILE_INPUT.FILENAME] to it[FILE_INPUT.FILE_HASH] }
        }
    }

    override fun getLatestChangelogByName(name: String): List<Changelog>
    {
        val latestVersion = reportRepository.getLatestVersion(name)
        return reportRepository.getDatedChangelogForReport(name, latestVersion.date)
    }

    override fun getChangelogByNameAndVersion(name: String, version: String): List<Changelog>
    {
        val basicVersion = reportRepository.getReportVersion(name, version)
        return reportRepository.getDatedChangelogForReport(basicVersion.name, basicVersion.date)
    }

    private fun mapToReportVersions(basicVersions: List<ReportVersionWithDescLatest>): List<ReportVersionWithDescCustomFieldsLatestParamsTags>
    {
        val versionIds = basicVersions.map { it.id }
        val allCustomFields = reportRepository.getAllCustomFields()
        val customFieldsForVersions = reportRepository.getCustomFieldsForVersions(versionIds)

        val parametersForVersions = reportRepository.getParametersForVersions(versionIds)

        val allVersionTags = tagRepository.getVersionTags(versionIds)
        val allReportTags = tagRepository.getReportTagsForVersions(versionIds)
        val allOrderlyTags = tagRepository.getOrderlyTagsForVersions(versionIds)

        return basicVersions.map {
            val versionId = it.id

            val versionCustomFields = mutableMapOf<String, String?>()

            versionCustomFields.putAll(allCustomFields)
            versionCustomFields.putAll(customFieldsForVersions[versionId] ?: mapOf())

            val versionParameters = parametersForVersions[versionId] ?: mapOf()

            val versionTags = allVersionTags[versionId] ?: listOf()
            val reportTags = allReportTags[versionId] ?: listOf()
            val orderlyTags = allOrderlyTags[versionId] ?: listOf()

            ReportVersionWithDescCustomFieldsLatestParamsTags(it,
                    versionCustomFields,
                    versionParameters,
                    (versionTags + reportTags + orderlyTags).distinct().sorted())
        }
    }

    private fun getDataInfo(name: String, version: String): List<DataInfo>
    {
        reportRepository.getReportVersion(name, version)
        JooqContext().use {
            return it.dsl.select(
                    REPORT_VERSION_DATA.NAME,
                    DATA.SIZE_CSV,
                    DATA.SIZE_RDS)
                    .from(REPORT_VERSION_DATA)
                    .innerJoin(DATA)
                    .on(REPORT_VERSION_DATA.HASH.eq(DATA.HASH))
                    .where(REPORT_VERSION_DATA.REPORT_VERSION.eq(version))
                    .fetch()
                    .map { r -> DataInfo(r[REPORT_VERSION_DATA.NAME], r[DATA.SIZE_CSV], r[DATA.SIZE_RDS]) }
        }
    }

    private fun getResourceFiles(name: String, version: String): List<FileInfo>
    {
        reportRepository.getReportVersion(name, version)
        return JooqContext().use { ctx ->
            ctx.dsl.select(FILE_INPUT.FILENAME, FILE.SIZE)
                    .from(FILE_INPUT)
                    .innerJoin(FILE)
                    .on(FILE_INPUT.FILE_HASH.eq(FILE.HASH))
                    .where(FILE_INPUT.REPORT_VERSION.eq(version))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.RESOURCE.toString()))
                    .fetch()
                    .map { FileInfo(it[FILE_INPUT.FILENAME], it[FILE.SIZE]) }

        }
    }
}
