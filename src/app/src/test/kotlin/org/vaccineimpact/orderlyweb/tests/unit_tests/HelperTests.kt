package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class HelperTests : TeamcityTests()
{
    @Test
    fun `html can render in browser`()
    {
        val html = canRenderInBrowser("test.html")
        val htm = canRenderInBrowser("test.htm")

        Assertions.assertThat(html).isTrue()
        Assertions.assertThat(htm).isTrue()
    }

    @Test
    fun `pdf can render in browser`()
    {
        val pdf = canRenderInBrowser("test.pdf")

        Assertions.assertThat(pdf).isTrue()
    }

    @Test
    fun `images can render in browser`()
    {
        val png = canRenderInBrowser("test.png")
        val gif = canRenderInBrowser("test.gif")
        val jpeg = canRenderInBrowser("test.jpeg")
        val jpg = canRenderInBrowser("test.jpg")
        val JPG = canRenderInBrowser("test.JPG")
        val svg = canRenderInBrowser("test.svg")

        Assertions.assertThat(png).isTrue()
        Assertions.assertThat(gif).isTrue()
        Assertions.assertThat(jpg).isTrue()
        Assertions.assertThat(jpeg).isTrue()
        Assertions.assertThat(JPG).isTrue()
        Assertions.assertThat(svg).isTrue()
    }
}