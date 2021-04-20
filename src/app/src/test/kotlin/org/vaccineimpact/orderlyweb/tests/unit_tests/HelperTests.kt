package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.*


class HelperTests
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
        val bmp = canRenderInBrowser("test.BMP")

        Assertions.assertThat(png).isTrue()
        Assertions.assertThat(gif).isTrue()
        Assertions.assertThat(jpg).isTrue()
        Assertions.assertThat(jpeg).isTrue()
        Assertions.assertThat(JPG).isTrue()
        Assertions.assertThat(svg).isTrue()
        Assertions.assertThat(bmp).isTrue()
    }

    @Test
    fun `byteCountToDisplaySize returns expected formatted strings`()
    {
        Assertions.assertThat(byteCountToDisplaySize(11)).isEqualTo("11 bytes")

        Assertions.assertThat(byteCountToDisplaySize(1024)).isEqualTo("1.0 KB")
        Assertions.assertThat(byteCountToDisplaySize(1100)).isEqualTo("1.1 KB")
        Assertions.assertThat(byteCountToDisplaySize(1120)).isEqualTo("1.1 KB")
        Assertions.assertThat(byteCountToDisplaySize(30720)).isEqualTo("30 KB")
        Assertions.assertThat(byteCountToDisplaySize(307200)).isEqualTo("300 KB")
        Assertions.assertThat(byteCountToDisplaySize(337920)).isEqualTo("330 KB")

        Assertions.assertThat(byteCountToDisplaySize(1150000)).isEqualTo("1.1 MB")

        Assertions.assertThat(byteCountToDisplaySize(1150000000)).isEqualTo("1.1 GB")
    }

    @Test
    fun `guessFileType returns expected file types`()
    {
        Assertions.assertThat(guessFileType("something.bmp")).isEqualTo("image/bmp")
        Assertions.assertThat(guessFileType("something.csv")).isEqualTo("text/csv")
        Assertions.assertThat(guessFileType("something.gif")).isEqualTo("image/gif")
        Assertions.assertThat(guessFileType("something.jpeg")).isEqualTo("image/jpg")
        Assertions.assertThat(guessFileType("something.jpg")).isEqualTo("image/jpg")
        Assertions.assertThat(guessFileType("something.png")).isEqualTo("image/png")
        Assertions.assertThat(guessFileType("something.svg")).isEqualTo("image/svg+xml")
        Assertions.assertThat(guessFileType("something.pdf")).isEqualTo("application/pdf")
        Assertions.assertThat(guessFileType("something.htm")).isEqualTo("text/html")
        Assertions.assertThat(guessFileType("something.html")).isEqualTo("text/html")
        Assertions.assertThat(guessFileType("something.css")).isEqualTo("text/css")
        Assertions.assertThat(guessFileType("something.xls")).isEqualTo("application/octet-stream")

        Assertions.assertThat(guessFileType("SOMETHING.BMP")).isEqualTo("image/bmp")
    }

    @Test
    fun `getFriendlyDateTime returns expected string`()
    {
        val str = "2020-06-08 12:30"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateTime = LocalDateTime.parse(str, formatter)

        Assertions.assertThat(getFriendlyDateTime(dateTime)).isEqualTo("Mon Jun 08 2020, 12:30")
    }

    @Test
    fun `getRelativeFriendlyDateTime returns expected strings`()
    {
        var date = Date().toInstant().minus(2, ChronoUnit.DAYS)
        var result = getFriendlyRelativeDateTime(Date.from(date))
        Assertions.assertThat(result).isEqualTo("2 days ago")

        date = Date().toInstant().minus(1, ChronoUnit.HOURS)
        result = getFriendlyRelativeDateTime(Date.from(date))
        Assertions.assertThat(result).isEqualTo("1 hour ago")
    }

    @Test
    fun `can transform json string to map`()
    {
        val jsonString = "{'name': 'value'}"
        val result: Map<String, String> = jsonToStringMap(jsonString)
        Assertions.assertThat(result).isEqualTo(mapOf("name" to "value"))
    }
}
