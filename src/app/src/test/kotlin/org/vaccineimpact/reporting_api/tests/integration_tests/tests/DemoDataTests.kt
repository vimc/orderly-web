package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

class DemoDataTests : IntegrationTest()
{
    companion object
    {
        const val MINIMAL_REPORT_NAME = "minimal"
        const val OTHER_REPORT_NAME = "other"
        const val CHANGELOG_REPORT_NAME = "changelog"
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
        assertThat(dataArray[0].asText()).isNotBlank()
        assertThat(dataArray[1].asText()).isNotBlank()
        assertThat(dataArray[2].asText()).isNotBlank()
    }

    @Test
    fun `can get demo report version data`()
    {
        val reportVersion = getLatestReportVersion(OTHER_REPORT_NAME)

        val response = requestHelper.get("/reports/$OTHER_REPORT_NAME/versions/$reportVersion/",
                user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data is ObjectNode)
        val dataObj = data as ObjectNode

        assertExpectedOtherReportVersionProperties(dataObj, reportVersion)

        assertThat(dataObj.get("displayname").asText()).isEqualTo("another report")

        assertThat(dataObj.get("description").asText()).isEqualTo("An extended comment field.  This can be quite long.  This is not so long though, but long enough I'm sure.")
        assertThat(dataObj.get("comment").asText()).isEqualTo("This is another comment")

        val dataFieldObj = dataObj.get("data") as ObjectNode
        assertThat(dataFieldObj.get("extract").asText()).isEqualTo("SELECT name, number FROM thing WHERE number > ?nmin")


        val artefactsArray = dataObj.get("artefacts") as ArrayNode
        val dataArtefactObj = (artefactsArray[0] as ObjectNode).get("data") as ObjectNode
        assertThat(dataArtefactObj.get("description").asText()).isEqualTo("A summary table")
        val fileNamesArray = dataArtefactObj.get("filenames") as ArrayNode
        assertThat(fileNamesArray[0].asText()).isEqualTo("summary.csv")

        val parametersObj = dataObj.get("parameters") as ObjectNode
        assertThat(parametersObj.get("nmin").asInt()).isEqualTo(0)

        assertThat(dataObj.get("hash_orderly").asText()).isEqualTo("5dfd610af385c346ede42a5802b1a3bf")
        assertThat(dataObj.get("hash_input").asText()).isEqualTo("360533871dcc18a93868649c5b55b3f1")

        val hashResourcesObj = dataObj.get("hash_resources") as ObjectNode
        assertThat(hashResourcesObj.get("functions.R").asText()).isEqualTo("cceb0c1c68beaa96266c6f2e3445b423")

        val hashDataObj = dataObj.get("hash_data") as ObjectNode
        assertThat(hashDataObj.get("extract").asText()).isEqualTo("386f507375907a60176b717016f0a648")

        val hashArtefactsObj = dataObj.get("hash_artefacts") as ObjectNode
        assertThat(hashArtefactsObj.get("summary.csv").asText()).isEqualTo("3a9dcd5ad0326386af938cf3a4b395cc")
        assertThat(hashArtefactsObj.get("graph.png").asText()).isEqualTo("067a7300a693861283e7479dfa7857d2")

        assertThat(dataObj.get("script").asText()).isEqualTo("script.R")

        assertThat(dataObj.get("hash_script").asText()).isEqualTo("ba1490c1ef2934c00b03e6fb5fdb5248")

    }

    @Test
    fun `can get demo report version details`()
    {
        val reportVersion = getLatestReportVersion(OTHER_REPORT_NAME)
        val response = requestHelper.get("/reports/$OTHER_REPORT_NAME/versions/$reportVersion/details",
                user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data is ObjectNode)
        val dataObj = data as ObjectNode

        assertExpectedOtherReportVersionProperties(dataObj, reportVersion)

        assertThat(dataObj.get("script").asText()).isEqualTo("script.R")

        assertThat(dataObj.get("hash_script").asText()).isEqualTo("ba1490c1ef2934c00b03e6fb5fdb5248")
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
        val reportVersion = getLatestReportVersion(OTHER_REPORT_NAME)
        val versionObj = versionArray.find { it.get("id").asText() == reportVersion } as ObjectNode
        assertThat(versionObj.get("date").asText()).isEqualTo("2017-12-04T12:28:16Z")
        assertExpectedOtherReportVersionProperties(versionObj, reportVersion)

    }

    @Test
    fun `can get demo changelog data`()
    {
        val reportVersion = getLatestReportVersion(CHANGELOG_REPORT_NAME)
        val response = requestHelper.get("/reports/$CHANGELOG_REPORT_NAME/versions/$reportVersion/changelog/",
                user = requestHelper.fakeReviewer)

        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data is ArrayNode)
        val clArray = data as ArrayNode

        assertThat(clArray.size()).isEqualTo(3)

        val entry1 = clArray[0] as ObjectNode
        assertThat(entry1.get("report_version").asText()).isEqualTo(reportVersion)
        assertThat(entry1.get("label").asText()).isEqualTo("public")
        assertThat(entry1.get("from_file").asBoolean()).isTrue()
        assertThat(entry1.get("value").asText()).startsWith("Do you see any Teletubbies in here?")

        val entry2 = clArray[1] as ObjectNode
        assertThat(entry2.get("label").asText()).isEqualTo("public")
        assertThat(entry2.get("from_file").asBoolean()).isTrue()
        assertThat(entry2.get("value").asText()).startsWith("Now that we know who you are, I know who I am. I'm not a mistake!")

        val entry3 = clArray[2] as ObjectNode
        assertThat(entry3.get("label").asText()).isEqualTo("internal")
        assertThat(entry3.get("from_file").asBoolean()).isTrue()
        assertThat(entry3.get("value").asText()).startsWith("Well, the way they make shows is, they make one show.")

    }

    private fun assertExpectedOtherReportVersionProperties(dataObj : ObjectNode, reportVersion : String)
    {
        assertThat(dataObj.get("name").asText()).isEqualTo(OTHER_REPORT_NAME)
        assertThat(dataObj.get("id").asText()).isEqualTo(reportVersion)

        assertThat(dataObj.get("published").asInt()).isEqualTo(1)
        assertThat(dataObj.get("requester").asText()).isEqualTo("ACME")
        assertThat(dataObj.get("author").asText()).isEqualTo("Dr Serious")

    }

    private fun getLatestReportVersion(report: String) : String
    {
        //report versions are different every time the data is generated, so fetch whatever is there at the moment
        val response = requestHelper.get("/reports/$report",  user = requestHelper.fakeReviewer)

        val data = JSONValidator.getData(response.text)
        val dataArray = data as ArrayNode
        return dataArray[dataArray.size()-1].asText()


    }
}