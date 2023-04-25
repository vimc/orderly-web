package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.FreeMarkerTest
import org.vaccineimpact.orderlyweb.viewmodels.*

//This will also test the partials which the report-page template includes

class VersionPageTests: FreeMarkerTest("report-page.ftl")
{
    @Test
    fun `renders outline correctly`()
    {
        val doc = jsoupDocFor(VersionPageTestData.testModel)

        assertThat(doc.select(".nav-item")[0].text()).isEqualTo("Report")
        assertThat(doc.select(".nav-item")[1].text()).isEqualTo("Metadata")
        assertThat(doc.select(".nav-item")[2].text()).isEqualTo("Downloads")
        assertThat(doc.select(".nav-item")[3].text()).isEqualTo("Changelog")

        assertThat(doc.selectFirst("#report-tab").hasClass("tab-pane active pt-4 pt-md-1")).isTrue()
        assertThat(doc.selectFirst("#metadata-tab").hasClass("tab-pane pt-4 pt-md-1")).isTrue()
        assertThat(doc.selectFirst("#downloads-tab").hasClass("tab-pane pt-4 pt-md-1")).isTrue()
        assertThat(doc.selectFirst("#changelog-tab").hasClass("tab-pane pt-4 pt-md-1")).isTrue()
    }

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val doc = jsoupDocFor(VersionPageTestData.testModel)
        val breadcrumbs = doc.select(".crumb-item")

        assertThat(breadcrumbs.count()).isEqualTo(1)
        assertThat(breadcrumbs.first().child(0).text()).isEqualTo("name")
        assertThat(breadcrumbs.first().child(0).attr("href")).isEqualTo("url")
    }

    @Test
    fun `renders version switcher option with correct selected attribute`()
    {
        val fakeVersions = listOf(VersionPickerViewModel("/", "Tue Jan 03 2017, 14:30", false),
                VersionPickerViewModel("/", "Mon Jan 02 2017, 12:30", true))

        val doc = jsoupDocFor(VersionPageTestData.testModel.copy(versions = fakeVersions))
        val options = doc.select("#report-version-switcher option")

        assertThat(options.count()).isEqualTo(2)
        assertThat(options[0].hasAttr("selected")).isEqualTo(false)
        assertThat(options[1].hasAttr("selected")).isEqualTo(true)
    }

    @Test
    fun `reviewers see publish switch`()
    {
        val appViewModel = VersionPageTestData.testDefaultModel.copy(isReviewer = true)
        val mockModel = VersionPageTestData.testModel.copy(appViewModel = appViewModel)
        val htmlResponse = htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        assertThat(publishSwitch).isNotNull()
    }

    @Test
    fun `non reviewers do not see publish switch`()
    {
        val appViewModel = VersionPageTestData.testDefaultModel.copy(isReviewer = false)
        val mockModel = VersionPageTestData.testModel.copy(appViewModel = appViewModel)
        val htmlResponse = htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        assertThat(publishSwitch).isNull()
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
        val mockModel = VersionPageTestData.testModel.copy(appViewModel = defaultModel)
        val htmlResponse = htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        assertThat(publishSwitch).isNotNull()
    }

    @Test
    fun `runners see run report`()
    {
        val mockModel = VersionPageTestData.testModel.copy(isRunner = true)
        val htmlResponse = htmlPageResponseFor(mockModel)

        val runReport = htmlResponse.getElementById("runReportVueApp")
        assertThat(runReport).isNotNull()
    }

    @Test
    fun `non runners do not see run report`()
    {
        val mockModel = VersionPageTestData.testModel.copy(isRunner = false)

        val htmlResponse = htmlPageResponseFor(mockModel)

        val deps = htmlResponse.getElementById("runReportVueApp")
        assertThat(deps).isNull()
    }

    @Test
    fun `runners see dependencies`()
    {
        val mockModel = VersionPageTestData.testModel.copy(isRunner = true)

        val htmlResponse = htmlPageResponseFor(mockModel)

        val deps = htmlResponse.getElementById("reportDependenciesVueApp")
        assertThat(deps).isNotNull()
    }

    @Test
    fun `non runners do not see dependencies`()
    {
        val mockModel = VersionPageTestData.testModel.copy(isRunner = false)

        val htmlResponse = htmlPageResponseFor(mockModel)

        val runReport = htmlResponse.getElementById("reportDependenciesVueApp")
        assertThat(runReport).isNull()
    }

    @Test
    fun `report readers are shown if user is admin`()
    {
        val appViewModel = VersionPageTestData.testDefaultModel.copy(isAdmin = true)
        val mockModel = VersionPageTestData.testModel.copy(appViewModel = appViewModel)

        val htmlResponse = htmlPageResponseFor(mockModel)
        val doc = jsoupDocFor(mockModel)

        val reportReaders = htmlResponse.getElementById("reportReadersListVueApp")
        assertThat(reportReaders).isNotNull()
        assertThat(doc.selectFirst("#reportReadersListVueApp label").text())
                .contains("Global read access")
        assertThat(doc.select("#reportReadersListVueApp label")[1].text())
                .isEqualTo("Specific read access")
    }

    @Test
    fun `report readers are not shown if user is not admin`()
    {
        val appViewModel = VersionPageTestData.testDefaultModel.copy(isAdmin = false)
        val mockModel = VersionPageTestData.testModel.copy(appViewModel = appViewModel)

        val htmlResponse = htmlPageResponseFor(mockModel)

        val reportReaders = htmlResponse.getElementById("reportReadersListVueApp")
        assertThat(reportReaders).isNull()
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
        val mockModel = VersionPageTestData.testModel.copy(appViewModel = defaultModel)
        val htmlResponse = htmlPageResponseFor(mockModel)

        val reportReaders = htmlResponse.getElementById("reportReadersListVueApp")
        assertThat(reportReaders).isNull()
    }
}
