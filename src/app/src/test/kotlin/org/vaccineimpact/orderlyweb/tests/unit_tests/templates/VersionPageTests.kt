package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.equalToCompressingWhiteSpace
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ArtefactFormat
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.*
import org.xmlmatchers.XmlMatchers.hasXPath
import java.sql.Timestamp

//This will also test the partials which the report-page template includes

class VersionPageTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("report-page.ftl")
    }

    private val testReport = ReportVersionDetails(name = "r1",
            displayName = "r1 display",
            id = "r1-v1",
            published = true,
            date = Timestamp(System.currentTimeMillis()).toInstant(),
            author = "an author",
            requester = "a requester",
            description = "description",
            artefacts = listOf(),
            resources = listOf(),
            dataHashes = mapOf())

    private val testArtefactViewModels = listOf(
            ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact1", listOf()),
                    listOf(
                            DownloadableFileViewModel("a1file1.png", "http://a1file1"),
                            DownloadableFileViewModel("a1file2.pdf", "http://a1file2")
                    ),
                    "inlinesrc.png"
            ),
            ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact2", listOf()),
                    listOf(
                            DownloadableFileViewModel("a2file1.xls", "http://a2file1")
                    ),
                    null
            )
    )

    private val testDataLinks = listOf(
            InputDataViewModel("key1",
                    DownloadableFileViewModel("key1.csv", "http://key1/csv"),
                    DownloadableFileViewModel("key1.rds", "http://key1/rds")),
            InputDataViewModel("key2",
                    DownloadableFileViewModel("key2.csv", "http://key2/csv"),
                    DownloadableFileViewModel("key2.rds", "http://key2/rds"))
    )

    private val testResources = listOf(
            DownloadableFileViewModel("resource1.csv", "http://resource1/csv"),
            DownloadableFileViewModel("resource2.csv", "http://resource2/csv")
    )

    private val testModel = ReportVersionPageViewModel(
            testReport,
            "/testFocalArtefactUrl",
            false,
            false,
            false,
            testArtefactViewModels,
            testDataLinks,
            testResources,
            DownloadableFileViewModel("zipFileName", "http://zipFileUrl"),
            listOf(),
            listOf(),
            listOf(Breadcrumb("name", "url")),
            true,
            "userName")

    @Test
    fun `renders outline correctly`()
    {
        val doc = template.jsoupDocFor(testModel)

        Assertions.assertThat(doc.select(".nav-item")[0].text()).isEqualTo("Report")
        Assertions.assertThat(doc.select(".nav-item")[1].text()).isEqualTo("Downloads")
        Assertions.assertThat(doc.select(".nav-item")[2].text()).isEqualTo("Changelog")

        Assertions.assertThat(doc.selectFirst("#report-tab").hasClass("tab-pane active pt-4 pt-md-1")).isTrue()
        Assertions.assertThat(doc.selectFirst("#downloads-tab").hasClass("tab-pane pt-4 pt-md-1")).isTrue()
        Assertions.assertThat(doc.selectFirst("#changelog-tab").hasClass("tab-pane pt-4 pt-md-1")).isTrue()
    }

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val doc = template.jsoupDocFor(testModel)
        val breadcrumbs = doc.select(".crumb-item")

        Assertions.assertThat(breadcrumbs.count()).isEqualTo(1)
        Assertions.assertThat(breadcrumbs.first().child(0).text()).isEqualTo("name")
        Assertions.assertThat(breadcrumbs.first().child(0).attr("href")).isEqualTo("url")
    }

    @Test
    fun `renders version switcher option with correct selected attribute`()
    {
        val fakeVersions = listOf(VersionPickerViewModel("/", "Tue Jan 03 2017, 14:30", false),
                VersionPickerViewModel("/", "Mon Jan 02 2017, 12:30", true))

        val doc = template.jsoupDocFor(testModel.copy(versions = fakeVersions))
        val options = doc.select("#report-version-switcher option")

        Assertions.assertThat(options.count()).isEqualTo(2)
        Assertions.assertThat(options[0].hasAttr("selected")).isEqualTo(false)
        Assertions.assertThat(options[1].hasAttr("selected")).isEqualTo(true)
    }

    @Test
    fun `renders report tab correctly`()
    {
        val xmlResponse = template.xmlResponseFor(testModel)

        val xPathRoot = "//div[@id='report-tab']"

        assertThat(xmlResponse, hasXPath("$xPathRoot/h1/text()",
                equalToCompressingWhiteSpace("r1 display")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/p[1]/text()",
                equalToCompressingWhiteSpace("r1-v1")))

        assertThat(xmlResponse, hasXPath("$xPathRoot/iframe/@src", equalTo("/testFocalArtefactUrl")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/div[@class='text-right']/a/text()",
                equalToCompressingWhiteSpace("View fullscreen")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/div[@class='text-right']/a/@href", equalTo("/testFocalArtefactUrl")))
    }

    @Test
    fun `renders download tab title correctly`()
    {
        val xmlResponse = template.xmlResponseFor(testModel)

        val xPathRoot = "//div[@id='downloads-tab']"

        assertThat(xmlResponse, hasXPath("$xPathRoot/h1/text()",
                equalToCompressingWhiteSpace("r1 display")))
    }

    @Test
    fun `renders download tab artefacts correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(testModel)

        val artefactsEl = jsoupDoc.select("#artefacts")
        val artefactCards = artefactsEl.select(".card")
        Assertions.assertThat(artefactCards.count()).isEqualTo(2)

        val artefactEl1 = artefactCards[0]
        Assertions.assertThat(artefactEl1.select(".card-header").text()).isEqualTo("artefact1")
        Assertions.assertThat(artefactEl1.select("img").attr("src")).isEqualTo("inlinesrc.png")
        val artefact1FileLinks = artefactEl1.select(".card-body div a")

        Assertions.assertThat(artefact1FileLinks.count()).isEqualTo(2)
        Assertions.assertThat(artefact1FileLinks[0].attr("href")).isEqualTo("http://a1file1")
        Assertions.assertThat(artefact1FileLinks[0].text()).isEqualTo("a1file1.png")
        Assertions.assertThat(artefact1FileLinks[0].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(artefact1FileLinks[1].attr("href")).isEqualTo("http://a1file2")
        Assertions.assertThat(artefact1FileLinks[1].text()).isEqualTo("a1file2.pdf")
        Assertions.assertThat(artefact1FileLinks[1].select("span.download-icon").count()).isEqualTo(1)


        val artefactEl2 = artefactCards[1]
        Assertions.assertThat(artefactEl2.select(".card-header").text()).isEqualTo("artefact2")
        Assertions.assertThat(artefactEl2.select("img").count()).isEqualTo(0)
        val artefact2FileLinks = artefactEl2.select(".card-body div a")
        Assertions.assertThat(artefact2FileLinks.count()).isEqualTo(1)
        Assertions.assertThat(artefact2FileLinks[0].attr("href")).isEqualTo("http://a2file1")
        Assertions.assertThat(artefact2FileLinks[0].text()).isEqualTo("a2file1.xls")
    }

    @Test
    fun `renders download tab data links correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(testModel)

        val linksEl = jsoupDoc.select("#data-links")
        Assertions.assertThat(linksEl.select(".card-header").text()).isEqualTo("Input data to the report")

        val linkRows = linksEl.select(".row")
        Assertions.assertThat(linkRows.count()).isEqualTo(2)

        val linkRow1 = linkRows[0]
        Assertions.assertThat(linkRow1.select(".data-link-key").text()).isEqualTo("key1")
        val linkRow1Csv = linkRow1.select("ul li")[0]
        Assertions.assertThat(linkRow1Csv.select("a").attr("href")).isEqualTo("http://key1/csv")
        Assertions.assertThat(linkRow1Csv.select("a").text()).isEqualTo("key1.csv")
        val linkRow1Rds = linkRow1.select("ul li")[1]
        Assertions.assertThat(linkRow1Rds.select("a").attr("href")).isEqualTo("http://key1/rds")
        Assertions.assertThat(linkRow1Rds.select("a").text()).isEqualTo("key1.rds")

        val linkRow2 = linkRows[1]
        Assertions.assertThat(linkRow2.select(".data-link-key").text()).isEqualTo("key2")
        val linkRow2Csv = linkRow2.select("ul li")[0]
        Assertions.assertThat(linkRow2Csv.select("a").attr("href")).isEqualTo("http://key2/csv")
        Assertions.assertThat(linkRow2Csv.select("a").text()).isEqualTo("key2.csv")
        val linkRow2Rds = linkRow2.select("ul li")[1]
        Assertions.assertThat(linkRow2Rds.select("a").attr("href")).isEqualTo("http://key2/rds")
        Assertions.assertThat(linkRow2Rds.select("a").text()).isEqualTo("key2.rds")
    }

    @Test
    fun `does not render download tab data links if none in model`()
    {
        val testModel = testModel.copy(dataLinks = listOf())

        val jsoupDoc = template.jsoupDocFor(testModel)

        val linksEl = jsoupDoc.select("#data-links")
        Assertions.assertThat(linksEl.count()).isEqualTo(0)
    }

    @Test
    fun `renders resources correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(testModel)

        val resourcesEl = jsoupDoc.select("#resources")
        Assertions.assertThat(resourcesEl.select(".card-header").text()).isEqualTo("Resources")

        val resourceLinks = resourcesEl.select(".card-body div a")
        Assertions.assertThat(resourceLinks.count()).isEqualTo(2)
        Assertions.assertThat(resourceLinks[0].attr("href")).isEqualTo("http://resource1/csv")
        Assertions.assertThat(resourceLinks[0].text()).isEqualTo("resource1.csv")
        Assertions.assertThat(resourceLinks[0].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(resourceLinks[1].attr("href")).isEqualTo("http://resource2/csv")
        Assertions.assertThat(resourceLinks[1].text()).isEqualTo("resource2.csv")
        Assertions.assertThat(resourceLinks[1].select("span.download-icon").count()).isEqualTo(1)
    }

    @Test
    fun `renders zipfile correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(testModel)

        val zipFileEl = jsoupDoc.select("#zip-file")
        val zipFileLink = zipFileEl.select("a")
        Assertions.assertThat(zipFileLink.attr("href")).isEqualTo("http://zipFileUrl")
        Assertions.assertThat(zipFileLink.text()).isEqualTo("zipFileName")
        Assertions.assertThat(zipFileLink.select("span.download-icon").count()).isEqualTo(1)
    }

    @Test
    fun `does not render download tab resources if none in model`()
    {
        val testModel = testModel.copy(resources = listOf())
        val jsoupDoc = template.jsoupDocFor(testModel)

        val resourcesEl = jsoupDoc.select("#resources")
        Assertions.assertThat(resourcesEl.count()).isEqualTo(0)
    }

    @Test
    fun `renders changelog tab title`()
    {
        val doc = template.jsoupDocFor(testModel)
        Assertions.assertThat(doc.select("#changelog-tab h3").text()).isEqualTo("Changelog")
    }

    @Test
    fun `renders changelog rows`()
    {
        val entries =  listOf(ChangelogItemViewModel("public", "something public"),
                ChangelogItemViewModel("internal", "something internal"))

        val changelog = listOf(ChangelogViewModel("14 Jun 2018", "v1", entries))

        val testModel = testModel.copy(changelog = changelog)
        val doc = template.jsoupDocFor(testModel)

        val rows = doc.select("#changelog-tab table tbody tr")

        Assertions.assertThat(rows.count()).isEqualTo(1)
        Assertions.assertThat(doc.select("#changelog-tab p").count()).isEqualTo(0)

        val cells = rows[0].select("td")
        Assertions.assertThat(cells[0].selectFirst("a").attr("href")).isEqualTo("/reports/r1/v1")
        Assertions.assertThat(cells[0].selectFirst("a").text()).isEqualTo("14 Jun 2018")

        Assertions.assertThat(cells[1].select("div")[0].className()).isEqualTo("badge changelog-label badge-public")
        Assertions.assertThat(cells[1].select("div")[1].className()).isEqualTo("changelog-item public")
        Assertions.assertThat(cells[1].select("div")[1].text()).isEqualTo("something public")

        Assertions.assertThat(cells[1].select("div")[2].className()).isEqualTo("badge changelog-label badge-internal")
        Assertions.assertThat(cells[1].select("div")[3].className()).isEqualTo("changelog-item internal")
        Assertions.assertThat(cells[1].select("div")[3].text()).isEqualTo("something internal")
    }


    @Test
    fun `renders no changelog message when changelog is empty`()
    {
        val doc = template.jsoupDocFor(testModel)

        Assertions.assertThat(doc.select("#changelog-tab table").count()).isEqualTo(0)
        Assertions.assertThat(doc.selectFirst("#changelog-tab p").text()).isEqualTo("There is no changelog for this report version")
    }

    @Test
    fun `admins see publish switch`()
    {
        val mockModel =  ReportVersionPageViewModel(
                testReport,
                "/testFocalArtefactUrl",
                isAdmin = true,
                isRunner = false,
                showPermissionManagement = false,
                artefacts = testArtefactViewModels,
                dataLinks = testDataLinks,
                resources = testResources,
                zipFile = DownloadableFileViewModel("zipFileName", "http://zipFileUrl"),
                versions = listOf(),
                changelog = listOf(),
                breadcrumbs = listOf(Breadcrumb("name", "url")),
                loggedIn = true,
                userName = "userName")

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNotNull()
    }

    @Test
    fun `non admins do not see publish switch`()
    {
        val mockModel =  ReportVersionPageViewModel(
                testReport,
                "/testFocalArtefactUrl",
                isAdmin = false,
                isRunner = false,
                showPermissionManagement = false,
                artefacts = testArtefactViewModels,
                dataLinks = testDataLinks,
                resources = testResources,
                zipFile = DownloadableFileViewModel("zipFileName", "http://zipFileUrl"),
                versions = listOf(),
                changelog = listOf(),
                breadcrumbs = listOf(Breadcrumb("name", "url")),
                loggedIn = true,
                userName = "userName")

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNull()
    }

    @Test
    fun `runners see run report`()
    {
        val mockModel = testModel.copy(isRunner = true)

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val runReport = htmlResponse.getElementById("runReportVueApp")
        Assertions.assertThat(runReport).isNotNull()
    }

    @Test
    fun `non runners do not see run report`()
    {
        val mockModel = testModel.copy(isRunner = false)

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val runReport = htmlResponse.getElementById("runReportVueApp")
        Assertions.assertThat(runReport).isNull()
    }

    @Test
    fun `report readers are shown if showPermissionManagement is true`()
    {
        val mockModel = testModel.copy(showPermissionManagement = true)

        val htmlResponse = template.htmlPageResponseFor(mockModel)
        val doc = template.jsoupDocFor(mockModel)

        val reportReaders = htmlResponse.getElementById("reportReadersListVueApp")
        Assertions.assertThat(reportReaders).isNotNull()
        Assertions.assertThat(doc.selectFirst("#reportReadersListVueApp label").text())
                .contains("Global read access")
        Assertions.assertThat(doc.select("#reportReadersListVueApp label")[1].text())
                .isEqualTo("Specific read access")
    }

    @Test
    fun `report readers are not shown if showPermissionManagement is false`()
    {
        val mockModel = testModel.copy(showPermissionManagement = false)

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val reportReaders = htmlResponse.getElementById("reportReadersListVueApp")
        Assertions.assertThat(reportReaders).isNull()
    }
}