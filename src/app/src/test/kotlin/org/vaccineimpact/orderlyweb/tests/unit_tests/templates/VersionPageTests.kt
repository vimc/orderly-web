package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.equalToCompressingWhiteSpace
import org.junit.ClassRule
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.Config
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
            description = "description",
            artefacts = listOf(),
            resources = listOf(),
            dataInfo = listOf(),
            parameterValues = mapOf("p1" to "v1", "p2" to "v2"))

    private val testArtefactViewModels = listOf(
            ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact1", listOf()),
                    listOf(
                            DownloadableFileViewModel("a1file1.png", "http://a1file1", 19876),
                            DownloadableFileViewModel("a1file2.pdf", "http://a1file2", 123)
                    ),
                    "inlinesrc.png"
            ),
            ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact2", listOf()),
                    listOf(
                            DownloadableFileViewModel("a2file1.xls", "http://a2file1", 2300000)
                    ),
                    null
            )
    )

    private val testDataLinks = listOf(
            InputDataViewModel("key1",
                    DownloadableFileViewModel("key1.csv", "http://key1/csv", 1720394),
                    DownloadableFileViewModel("key1.rds", "http://key1/rds", 4123451)),
            InputDataViewModel("key2",
                    DownloadableFileViewModel("key2.csv", "http://key2/csv", 3123),
                    DownloadableFileViewModel("key2.rds", "http://key2/rds", 4562))
    )

    private val testResources = listOf(
            DownloadableFileViewModel("resource1.csv", "http://resource1/csv", 1234),
            DownloadableFileViewModel("resource2.csv", "http://resource2/csv", 2345)
    )

    private val testDefaultModel = DefaultViewModel(true, "username",
            isReviewer = false,
            isAdmin = false,
            breadcrumbs = listOf(Breadcrumb("name", "url")))

    private val testModel = ReportVersionPageViewModel(
            testReport,
            "/testFocalArtefactUrl",
            false,
            testArtefactViewModels,
            testDataLinks,
            testResources,
            DownloadableFileViewModel("zipFileName", "http://zipFileUrl", null),
            listOf(),
            listOf(),
            "p1=v1, p2=v2",
            testDefaultModel)

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

        assertThat(xmlResponse, hasXPath("$xPathRoot/p[@id='param-values']", equalToCompressingWhiteSpace("Parameter values: p1=v1, p2=v2")))

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
        val artefact1FileSizeSpans = artefactEl1.select("span.file-size")

        Assertions.assertThat(artefact1FileLinks.count()).isEqualTo(2)
        Assertions.assertThat(artefact1FileSizeSpans.count()).isEqualTo(2)

        Assertions.assertThat(artefact1FileLinks[0].attr("href")).isEqualTo("http://a1file1")
        Assertions.assertThat(artefact1FileLinks[0].text()).isEqualTo("a1file1.png")
        Assertions.assertThat(artefact1FileLinks[0].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(artefact1FileSizeSpans[0].text()).isEqualTo("(19 KB)")

        Assertions.assertThat(artefact1FileLinks[1].attr("href")).isEqualTo("http://a1file2")
        Assertions.assertThat(artefact1FileLinks[1].text()).isEqualTo("a1file2.pdf")
        Assertions.assertThat(artefact1FileLinks[1].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(artefact1FileSizeSpans[1].text()).isEqualTo("(123 bytes)")


        val artefactEl2 = artefactCards[1]
        Assertions.assertThat(artefactEl2.select(".card-header").text()).isEqualTo("artefact2")
        Assertions.assertThat(artefactEl2.select("img").count()).isEqualTo(0)

        val artefact2FileLinks = artefactEl2.select(".card-body div a")
        val artefact2FileSizeSpans = artefactEl2.select("span.file-size")
        Assertions.assertThat(artefact2FileLinks.count()).isEqualTo(1)
        Assertions.assertThat(artefact2FileSizeSpans.count()).isEqualTo(1)

        Assertions.assertThat(artefact2FileLinks[0].attr("href")).isEqualTo("http://a2file1")
        Assertions.assertThat(artefact2FileLinks[0].text()).isEqualTo("a2file1.xls")
        Assertions.assertThat(artefact2FileSizeSpans[0].text()).isEqualTo("(2.2 MB)")
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
        //Currently failing because of weird commons ui rounding behaviour...
        Assertions.assertThat(linkRow1Csv.select("span.file-size").text()).isEqualTo("(1.6 MB)")

        val linkRow1Rds = linkRow1.select("ul li")[1]
        Assertions.assertThat(linkRow1Rds.select("a").attr("href")).isEqualTo("http://key1/rds")
        Assertions.assertThat(linkRow1Rds.select("a").text()).isEqualTo("key1.rds")
        Assertions.assertThat(linkRow1Rds.select("span.file-size").text()).isEqualTo("(3.9 MB)")

        val linkRow2 = linkRows[1]
        Assertions.assertThat(linkRow2.select(".data-link-key").text()).isEqualTo("key2")
        val linkRow2Csv = linkRow2.select("ul li")[0]
        Assertions.assertThat(linkRow2Csv.select("a").attr("href")).isEqualTo("http://key2/csv")
        Assertions.assertThat(linkRow2Csv.select("a").text()).isEqualTo("key2.csv")
        Assertions.assertThat(linkRow2Csv.select("span.file-size").text()).isEqualTo("(3.0 KB)")

        val linkRow2Rds = linkRow2.select("ul li")[1]
        Assertions.assertThat(linkRow2Rds.select("a").attr("href")).isEqualTo("http://key2/rds")
        Assertions.assertThat(linkRow2Rds.select("a").text()).isEqualTo("key2.rds")
        Assertions.assertThat(linkRow2Rds.select("span.file-size").text()).isEqualTo("(4.5 KB)")
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
        val resourceFileSizeSpans = resourcesEl.select(".card-body div span.file-size")

        Assertions.assertThat(resourceLinks.count()).isEqualTo(2)
        Assertions.assertThat(resourceFileSizeSpans.count()).isEqualTo(2)

        Assertions.assertThat(resourceLinks[0].attr("href")).isEqualTo("http://resource1/csv")
        Assertions.assertThat(resourceLinks[0].text()).isEqualTo("resource1.csv")
        Assertions.assertThat(resourceLinks[0].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(resourceFileSizeSpans[0].text()).isEqualTo("(1.2 KB)")

        Assertions.assertThat(resourceLinks[1].attr("href")).isEqualTo("http://resource2/csv")
        Assertions.assertThat(resourceLinks[1].text()).isEqualTo("resource2.csv")
        Assertions.assertThat(resourceLinks[1].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(resourceFileSizeSpans[1].text()).isEqualTo("(2.3 KB)")
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
        //should not have rendered size link, as no size info
        Assertions.assertThat(zipFileEl.select("span.file-link").count()).isEqualTo(0)
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
    fun `reviewers see publish switch`()
    {
        val mockModel = testModel.copy(appViewModel = testDefaultModel.copy(isReviewer = true))
        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNotNull()
    }

    @Test
    fun `non reviewers do not see publish switch`()
    {
        val mockModel = testModel.copy(appViewModel = testDefaultModel.copy(isReviewer = false))
        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNull()
    }

    @Test
    fun `not reviewers see publish switch if auth is not enabled`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(any())
            } doReturn false
        }
        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
            on { get("app.name") } doReturn "appName"
            on { get("app.url") } doReturn "http://app"
            on { get("app.email") } doReturn "email"
            on { get("app.logo") } doReturn "logo.png"
            on { get("montagu.url") } doReturn "montagu"
        }

        val defaultModel = DefaultViewModel(mockContext, IndexViewModel.breadcrumb, appConfig = mockConfig)
        val mockModel = testModel.copy(appViewModel = defaultModel)
        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNotNull()
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
    fun `non runners see run report if auth is not enabled`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(any())
            } doReturn false
        }

        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
            on { get("app.name") } doReturn "appName"
            on { get("app.url") } doReturn "http://app"
            on { get("app.email") } doReturn "email"
            on { get("app.logo") } doReturn "logo.png"
            on { get("montagu.url") } doReturn "montagu"
        }

        val defaultModel = DefaultViewModel(mockContext, IndexViewModel.breadcrumb, appConfig = mockConfig)
        val mockModel = testModel.copy(isRunner=false, appViewModel = defaultModel)
        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val runReport = htmlResponse.getElementById("runReportVueApp")
        Assertions.assertThat(runReport).isNotNull()
    }

    @Test
    fun `report readers are shown if user is admin`()
    {
        val mockModel = testModel.copy(appViewModel = testDefaultModel.copy(isAdmin = true))

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
    fun `report readers are not shown if user is not admin`()
    {
        val mockModel = testModel.copy(appViewModel = testDefaultModel.copy(isAdmin = false))

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val reportReaders = htmlResponse.getElementById("reportReadersListVueApp")
        Assertions.assertThat(reportReaders).isNull()
    }

    @Test
    fun `report readers are not shown if auth is not enabled`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(any())
            } doReturn true
        }
        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
            on { get("app.name") } doReturn "appName"
            on { get("app.url") } doReturn "http://app"
            on { get("app.email") } doReturn "email"
            on { get("app.logo") } doReturn "logo.png"
            on { get("montagu.url") } doReturn "montagu"
        }

        val defaultModel = DefaultViewModel(mockContext, IndexViewModel.breadcrumb, appConfig = mockConfig)
        val mockModel = testModel.copy(appViewModel = defaultModel)
        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val reportReaders = htmlResponse.getElementById("reportReadersListVueApp")
        Assertions.assertThat(reportReaders).isNull()
    }


}