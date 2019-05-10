package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.RequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.*
import org.vaccineimpact.orderlyweb.tests.integration_tests.validators.JSONValidator
import java.io.File

abstract class IntegrationTest : TeamcityTests()
{
    val requestHelper = RequestHelper()
    val JSONValidator = JSONValidator()

    companion object
    {
        var appStarted: Boolean = false

        @BeforeClass
        @JvmStatic
        fun startApp()
        {
            if (!appStarted)
            {
                appStarted = true
                main(emptyArray())
            }
        }
    }

    @Before
    fun createDatabase()
    {
        println("Copying database from: ${AppConfig()["db.template"]}")

        val newDbFile = File(AppConfig()["db.location"])
        val source = File(AppConfig()["db.template"])

        source.copyTo(newDbFile, true)
        Thread.sleep(1000)
    }

    @After
    fun deleteDatabases()
    {
        File(AppConfig()["db.location"]).delete()
    }

    protected fun assertSuccessful(response: Response)
    {
        Assertions.assertThat(response.statusCode)
                .isEqualTo(200)

        Assertions.assertThat(response.headers["Content-Encoding"]).isEqualTo("gzip")
    }

    protected fun assertSuccessfulWithResponseText(response: Response)
    {
        Assertions.assertThat(response.statusCode)
                .withFailMessage(response.text)
                .isEqualTo(200)

        Assertions.assertThat(response.headers["Content-Encoding"]).isEqualTo("gzip")
    }

    protected fun assertUnauthorized(response: Response, reportName: String)
    {
        Assertions.assertThat(response.statusCode)
                .isEqualTo(403)

        JSONValidator.validateError(response.text, "forbidden",
                "You do not have sufficient permissions to access this resource. Missing these permissions: report:${reportName}/reports.read")

    }

    protected fun assertJsonContentType(response: Response)
    {
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json;charset=utf-8")
    }

    protected fun assertHtmlContentType(response: Response)
    {
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/html")
    }

    protected fun assertUrlSecured(url: String,
        contentType: String,
        useWebBaseUrl: Boolean = true,
        emailWithPermissions: String = fakeGlobalReportReader(),
        emailWithoutPermissions: String = fakeNoPermssionsUser())
    {
        //Check that a GET endpoint returns 403 when access is attempted by user without required permissions,
        // and non-403 response when access is attempted by user with permissions

        var response = requestHelper.get(url, userEmail = emailWithPermissions,
                useWebBaseUrl = useWebBaseUrl, contentType = contentType)

        Assertions.assertThat(response.statusCode).isNotEqualTo(403)

        response = requestHelper.get(url, userEmail = emailWithoutPermissions,
                useWebBaseUrl = useWebBaseUrl, contentType = contentType)

        Assertions.assertThat(response.statusCode).isEqualTo(403)
    }
}