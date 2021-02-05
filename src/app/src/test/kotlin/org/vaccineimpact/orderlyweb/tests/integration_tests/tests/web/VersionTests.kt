package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.fromJoinPath
import org.vaccineimpact.orderlyweb.db.joinPath
import org.vaccineimpact.orderlyweb.models.FilePurpose
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod
import java.net.URLEncoder

class VersionTests : IntegrationTest() {
    @Test
    fun `only report reviewers can publish report version`() {
        val version = JooqContext().use {
            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[REPORT_VERSION.ID]
        val reportName = version[REPORT_VERSION.REPORT]

        val url = "/report/$reportName/version/$versionId/publish/"

        assertWebUrlSecured(url, setOf(
                ReifiedPermission("reports.review", Scope.Global())), method = HttpMethod.post,
                contentType = ContentTypes.json)
    }

    @Test
    fun `only report readers can get resource`() {
        val (report, url) = getAnyArtefactUrl()
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))), ContentTypes.binarydata)
    }

    @Test
    fun `only report readers can get artefact`() {
        val (report, url) = getAnyArtefactUrl()
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))), ContentTypes.binarydata)
    }

    @Test
    fun `only report readers can get zip file`() {
        val version = JooqContext().use {

            it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.ID, ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .fetchAny()
        }

        val versionId = version[ORDERLYWEB_REPORT_VERSION_FULL.ID]
        val reportName = version[ORDERLYWEB_REPORT_VERSION_FULL.REPORT]

        val url = "/report/$reportName/version/$versionId/all/"

        assertWebUrlSecured(url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", reportName))),
                ContentTypes.binarydata)
    }

    @Test
    fun `only report readers can get csv data`() {
        val (report, url) = getAnyDataUrl()
        assertWebUrlSecured("$url?type=csv", setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))),
                contentType = ContentTypes.binarydata)
    }

    @Test
    fun `only report readers can get rds data`() {
        val (report, url) = getAnyDataUrl()
        assertWebUrlSecured("$url?type=rds", setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))),
                contentType = ContentTypes.binarydata)
    }

    @Test
    fun `only report readers can get tags`() {
        val (report, url) = getAnyTagsUrl()
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.read",
                Scope.Specific("report", report))), ContentTypes.json)
    }

    private fun getAnyTagsUrl(): Pair<String, String> {
        val version = JooqContext().use {

            it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.ID, ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .fetchAny()
        }

        val versionId = version[REPORT_VERSION.ID]
        val reportName = version[REPORT_VERSION.REPORT]

        val url = "/report/$reportName/version/$versionId/tags/"
        return Pair(reportName, url)
    }

    private fun getAnyDataUrl(): Pair<String, String> {
        val data = JooqContext().use {

            it.dsl.select(REPORT_VERSION_DATA.NAME, ORDERLYWEB_REPORT_VERSION_FULL.REPORT, REPORT_VERSION_DATA.REPORT_VERSION)
                    .from(REPORT_VERSION_DATA)
                    .join(ORDERLYWEB_REPORT_VERSION_FULL)
                    .on(REPORT_VERSION_DATA.REPORT_VERSION.eq(ORDERLYWEB_REPORT_VERSION_FULL.ID))
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .fetchAny()
        }

        val report = data[REPORT_VERSION.REPORT]
        val version = data[REPORT_VERSION_DATA.REPORT_VERSION]
        val name = URLEncoder.encode(data[REPORT_VERSION_DATA.NAME], "UTF-8")

        return Pair(report, "/report/$report/version/$version/data/$name/")
    }

    private fun getAnyResourceUrl(): Pair<String, String> {
        val resource = JooqContext().use {

            it.dsl.select(FILE_INPUT.FILENAME, FILE_INPUT.REPORT_VERSION, REPORT_VERSION.REPORT)
                    .from(FILE_INPUT)
                    .join(ORDERLYWEB_REPORT_VERSION_FULL)
                    .on(FILE_INPUT.REPORT_VERSION.eq(ORDERLYWEB_REPORT_VERSION_FULL.ID))
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.RESOURCE.toString()))
                    .fetchAny()
        }

        val report = resource[ORDERLYWEB_REPORT_VERSION_FULL.REPORT]
        val version = resource[FILE_INPUT.REPORT_VERSION]
        val fileName = resource[FILE_INPUT.FILENAME]
        val encodedFileName = URLEncoder.encode(fileName, "UTF-8")

        return Pair(report, "/report/$report/version/$version/resources/$encodedFileName/")
    }

    private fun getAnyArtefactUrl(): Pair<String, String> {
        val resource = JooqContext().use {

            it.dsl.select(FILE_ARTEFACT.FILENAME, REPORT_VERSION_ARTEFACT.REPORT_VERSION, ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                    .fromJoinPath(FILE_ARTEFACT, REPORT_VERSION_ARTEFACT)
                    .join(ORDERLYWEB_REPORT_VERSION_FULL)
                    .on(REPORT_VERSION_ARTEFACT.REPORT_VERSION.eq(ORDERLYWEB_REPORT_VERSION_FULL.ID))
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .fetchAny()
        }

        val report = resource[ORDERLYWEB_REPORT_VERSION_FULL.REPORT]
        val version = resource[REPORT_VERSION_ARTEFACT.REPORT_VERSION]
        val fileName = resource[FILE_ARTEFACT.FILENAME]
        val encodedFileName = URLEncoder.encode(fileName, "UTF-8")

        return Pair(report, "/report/$report/version/$version/artefacts/$encodedFileName/")
    }
}