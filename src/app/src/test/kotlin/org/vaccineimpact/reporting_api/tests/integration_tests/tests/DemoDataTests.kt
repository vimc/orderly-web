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

        assertThat(dataObj.get("display_name").asText()).isEqualTo("another report")
        assertThat(dataObj.get("description").asText()).isEqualTo("An extended comment field.  This can be quite long.  This is not so long though, but long enough I'm sure.")

        val artefactsArray = dataObj.get("artefacts") as ArrayNode
        val dataArtefactObj = (artefactsArray[0] as ObjectNode)
        assertThat(dataArtefactObj.get("description").asText()).isEqualTo("A summary table")
        val fileNamesArray = dataArtefactObj.get("files") as ArrayNode
        assertThat(fileNamesArray[0].asText()).isEqualTo("summary.csv")
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
        assertThat(versionObj.get("date").asText()).isNotBlank()
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

        assertThat(clArray.size()).isEqualTo(4)

        val entry0 = clArray[0] as ObjectNode
        assertThat(entry0.get("report_version").asText()).isEqualTo(reportVersion)
        assertThat(entry0.get("label").asText()).isEqualTo("public")
        assertThat(entry0.get("from_file").asBoolean()).isTrue()
        assertThat(entry0.get("value").asText()).startsWith("You think water moves fast?")

        val entry1 = clArray[1] as ObjectNode
        assertThat(entry1.get("report_version").asText()).isEqualTo(reportVersion)
        assertThat(entry1.get("label").asText()).isEqualTo("public")
        assertThat(entry1.get("from_file").asBoolean()).isTrue()
        assertThat(entry1.get("value").asText()).startsWith("Do you see any Teletubbies in here?")

        val entry2 = clArray[2] as ObjectNode
        assertThat(entry2.get("label").asText()).isEqualTo("public")
        assertThat(entry2.get("from_file").asBoolean()).isTrue()
        assertThat(entry2.get("value").asText()).startsWith("Now that we know who you are, I know who I am. I'm not a mistake!")

        val entry3 = clArray[3] as ObjectNode
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