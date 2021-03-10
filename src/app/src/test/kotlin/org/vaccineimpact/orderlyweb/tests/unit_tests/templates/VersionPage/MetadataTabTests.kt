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
        assertThat(content.text()).isEqualToIgnoringWhitespace("No relevant metadata")
    }
}
