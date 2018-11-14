package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.gson.JsonObject

class DemoDataTests : IntegrationTest()
{
    companion object
    {
        const val MINIMAL_REPORT_NAME = "minimal"
        const val OTHER_REPORT_NAME = "other"
        const val CHANGELOG_REPORT_NAME = "changelog"

        const val OTHER_REPORT_VERSION = "20171121-201156-b9e7fb19"
        const val CHANGELOG_REPORT_VERSION = "20171202-074745-4f66ded4"
    }

    @Test
    fun `can get demo report data`()
    {
        val response = requestHelper.get("/reports/$MINIMAL_REPORT_NAME",  user = requestHelper.fakeReviewer)

        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data is ArrayNode)
        val dataArray = data as ArrayNode
        assertThat(dataArray.size()).isEqualTo(3)
        assertThat(dataArray[0].asText()).isEqualTo("20171112-084541-6e980032")
        assertThat(dataArray[1].asText()).isEqualTo("20171114-065304-e0146037")
        assertThat(dataArray[2].asText()).isEqualTo("20171118-162858-fbf862e4")
    }

    @Test
    fun `can get demo report version data`()
    {
        val response = requestHelper.get("/reports/$OTHER_REPORT_NAME/versions/$OTHER_REPORT_VERSION",
                user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data is ObjectNode)
        val dataObj = data as ObjectNode

        assertExpectedOtherReportVersionProperties(dataObj)

        assertThat(dataObj.get("date").asText()).isEqualTo("2017-11-21T20:11:56Z")
        assertThat(dataObj.get("description").asText()).isEqualTo("An extended comment field.  This can be quite long.  This is not so long though, but long enough I'm sure.")
        assertThat(dataObj.get("comment").asText()).isEqualTo("This is another comment")

        val dataFieldObj = dataObj.get("data") as ObjectNode
        assertThat(dataFieldObj.get("extract").asText()).isEqualTo("SELECT name, number FROM thing WHERE number > ?nmin")


        val artefactsArray = dataObj.get("artefacts") as ArrayNode
        val dataArtefactObj = (artefactsArray[0] as ObjectNode).get("data") as ObjectNode
        assertThat(dataArtefactObj.get("description").asText()).isEqualTo("A summary table")
        val fileNamesArray = dataArtefactObj.get("filenames") as ArrayNode
        assertThat(fileNamesArray[0].asText()).isEqualTo("summary.csv")

        assertThat(dataObj.get("script").asText()).isEqualTo("script.R")

        assertThat(dataObj.get("hash_script").asText()).isEqualTo("0e47057061019918a94f83570d06ef54")

        val parametersObj = dataObj.get("parameters") as ObjectNode
        assertThat(parametersObj.get("nmin").asInt()).isEqualTo(0)

        assertThat(dataObj.get("hash_orderly").asText()).isEqualTo("e69d1ff419b92c502e3712e86b3e1953")
        assertThat(dataObj.get("hash_input").asText()).isEqualTo("360533871dcc18a93868649c5b55b3f1")

        val hashResourcesObj = dataObj.get("hash_resources") as ObjectNode
        assertThat(hashResourcesObj.get("functions.R").asText()).isEqualTo("cceb0c1c68beaa96266c6f2e3445b423")

        val hashDataObj = dataObj.get("hash_data") as ObjectNode
        assertThat(hashDataObj.get("extract").asText()).isEqualTo("386f507375907a60176b717016f0a648")

        val hashArtefactsObj = dataObj.get("hash_artefacts") as ObjectNode
        assertThat(hashArtefactsObj.get("summary.csv").asText()).isEqualTo("23877598325ab7d45999349ef868a7f5")
        assertThat(hashArtefactsObj.get("graph.png").asText()).isEqualTo("7360cb2eed3327ff8a677b3598ed7343")

    }

    @Test
    fun `can get all demo version data`()
    {
        //This is hitting the Report_Version table rather than Orderly

        val response = requestHelper.get("/versions/", user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data is ArrayNode)
        val versionArray = data as ArrayNode

        //Check that we have one expected version which should contain values for all fields
        val versionObj = versionArray.find { it.get("id").asText() == OTHER_REPORT_VERSION } as ObjectNode
        assertThat(versionObj.get("date").asText()).isEqualTo("2017-11-21T20:11:56Z")
        assertExpectedOtherReportVersionProperties(versionObj)

    }

    @Test
    fun `can get demo changelog data`()
    {
        val response = requestHelper.get("/reports/$CHANGELOG_REPORT_NAME/versions/$CHANGELOG_REPORT_VERSION/changelog/",
                user = requestHelper.fakeReviewer)

        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data is ArrayNode)
        val clArray = data as ArrayNode

        assertThat(clArray.size()).isEqualTo(2)

        val entry1 = clArray[0] as ObjectNode
        assertThat(entry1.get("report_version").asText()).isEqualTo(CHANGELOG_REPORT_VERSION)
        assertThat(entry1.get("label").asText()).isEqualTo("public")
        assertThat(entry1.get("from_file").asBoolean()).isTrue()
        assertThat(entry1.get("value").asText()).startsWith("Now that we know who you are, I know who I am. I'm not a mistake!")

        val entry2 = clArray[1] as ObjectNode
        assertThat(entry2.get("report_version").asText()).isEqualTo(CHANGELOG_REPORT_VERSION)
        assertThat(entry2.get("label").asText()).isEqualTo("internal")
        assertThat(entry2.get("from_file").asBoolean()).isTrue()
        assertThat(entry2.get("value").asText()).startsWith("Well, the way they make shows is, they make one show.")

    }

    private fun assertExpectedOtherReportVersionProperties(dataObj : ObjectNode)
    {
        assertThat(dataObj.get("name").asText()).isEqualTo(OTHER_REPORT_NAME)
        assertThat(dataObj.get("id").asText()).isEqualTo(OTHER_REPORT_VERSION)

        assertThat(dataObj.get("display_name").asText()).isEqualTo("another report")
        assertThat(dataObj.get("published").asInt()).isEqualTo(0)
        assertThat(dataObj.get("requester").asText()).isEqualTo("ACME")
        assertThat(dataObj.get("author").asText()).isEqualTo("Dr Serious")
    }
}