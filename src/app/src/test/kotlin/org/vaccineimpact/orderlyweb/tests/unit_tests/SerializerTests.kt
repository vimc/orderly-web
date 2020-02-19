package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.github.salomonbrys.kotson.get
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.viewmodels.ReportRowViewModel

class SerializerTests
{
    @Test
    fun `Serializes ReportRowViewModel`()
    {
        val viewModel = ReportRowViewModel(1, 2,"reportName", "displayName", "id123",
                "version1", "Mon 12 Jan 2020", 1, true,
                mapOf("cust1" to "val1", "cust2" to "val2"))

        val sut = Serializer()

        val result = sut.toResult(viewModel)

        val jObj = Gson().fromJson(result, JsonObject::class.java)
        val jData = jObj.get("data")
        assertThat(jData.get("tt_key").asInt).isEqualTo(1)
        assertThat(jData.get("tt_parent").asInt).isEqualTo(2)
        assertThat(jData.get("name").asString).isEqualTo("reportName")
        assertThat(jData.get("display_name").asString).isEqualTo("displayName")
        assertThat(jData.get("id").asString).isEqualTo("id123")
        assertThat(jData.get("latest_version").asString).isEqualTo("version1")
        assertThat(jData.get("date").asString).isEqualTo("Mon 12 Jan 2020")
        assertThat(jData.get("num_versions").asInt).isEqualTo(1)
        assertThat(jData.get("published").asBoolean).isEqualTo(true)

        assertThat(jData.get("cust1").asString).isEqualTo("val1")
        assertThat(jData.get("cust2").asString).isEqualTo("val2")
    }
}