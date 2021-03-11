package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MetadataTabTests: BaseVersionPageTests()
{
    @Test
    fun `renders metadata tab title correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(VersionPageTestData.testModel)
        val title = jsoupDoc.select("#metadata-tab h1")
        assertThat(title.text()).isEqualToIgnoringWhitespace("r1 display")
    }

    @Test
    fun `renders metadata tab content correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(VersionPageTestData.testModel)
        val content = jsoupDoc.select("#metadata-tab .container")

        assertThat(content.select("#started-label").text()).isEqualTo("Started:")
        assertThat(content.select("#started-value").text()).isEqualTo("Mon 12 Jun 2020 14:23")
        assertThat(content.select("#elapsed-label").text()).isEqualTo("Elapsed:")
        assertThat(content.select("#elapsed-value").text()).isEqualTo("3 hours 2 minutes")
    }
}
