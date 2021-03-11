package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.viewmodels.ChangelogItemViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ChangelogViewModel

class ChangelogTabTests: BaseVersionPageTests()
{
    @Test
    fun `renders changelog tab title`()
    {
        val doc = template.jsoupDocFor(VersionPageTestData.testModel)
        assertThat(doc.select("#changelog-tab h3").text()).isEqualTo("Changelog")
    }

    @Test
    fun `renders changelog rows`()
    {
        val entries =  listOf(ChangelogItemViewModel("custom-public-label", "something public", "public"),
                ChangelogItemViewModel("custom-internal-label", "something internal", "internal"))

        val changelog = listOf(ChangelogViewModel("14 Jun 2018", "v1", entries))

        val testModel = VersionPageTestData.testModel.copy(changelog = changelog)
        val doc = template.jsoupDocFor(testModel)

        val rows = doc.select("#changelog-tab table tbody tr")

        assertThat(rows.count()).isEqualTo(1)
        assertThat(doc.select("#changelog-tab p").count()).isEqualTo(0)

        val cells = rows[0].select("td")
        assertThat(cells[0].selectFirst("a").attr("href")).isEqualTo("/reports/r1/v1")
        assertThat(cells[0].selectFirst("a").text()).isEqualTo("14 Jun 2018")

        assertThat(cells[1].select("div")[0].className()).isEqualTo("badge changelog-label badge-public")
        assertThat(cells[1].select("div")[1].className()).isEqualTo("changelog-item public")
        assertThat(cells[1].select("div")[1].text()).isEqualTo("something public")

        assertThat(cells[1].select("div")[2].className()).isEqualTo("badge changelog-label badge-internal")
        assertThat(cells[1].select("div")[3].className()).isEqualTo("changelog-item internal")
        assertThat(cells[1].select("div")[3].text()).isEqualTo("something internal")
    }


    @Test
    fun `renders no changelog message when changelog is empty`()
    {
        val doc = template.jsoupDocFor(VersionPageTestData.testModel)

        assertThat(doc.select("#changelog-tab table").count()).isEqualTo(0)
        assertThat(doc.selectFirst("#changelog-tab p").text()).isEqualTo("There is no changelog for this report version")
    }
}
