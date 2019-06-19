package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.integration_tests.WebPermissionChecker
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.WebRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.validators.JSONValidator
import spark.route.HttpMethod
import java.io.File

abstract class IntegrationTest : TeamcityTests()
{
    val apiRequestHelper = APIRequestHelper()
    val webRequestHelper = WebRequestHelper()

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

    protected fun assertWebUrlSecured(url: String, requiredPermissions: Set<ReifiedPermission>,
                                      contentType: String = ContentTypes.html,
                                      method: HttpMethod = HttpMethod.get,
                                      postData: Map<String, String>? = null)
    {
        val checker = WebPermissionChecker(url, requiredPermissions, contentType, method, postData)
        checker.checkPermissionsAreSufficient()

        for (permission in requiredPermissions)
        {
            checker.checkPermissionIsRequired(permission)
        }
    }
}