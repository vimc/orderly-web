package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.BeforeAll
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.OrderlyServerClient
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.GitCommit
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.JSONValidator
import org.vaccineimpact.orderlyweb.test_helpers.http.Response
import org.vaccineimpact.orderlyweb.tests.integration_tests.APIPermissionChecker
import org.vaccineimpact.orderlyweb.tests.integration_tests.WebPermissionChecker
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.WebRequestHelper
import spark.route.HttpMethod
import java.io.File

abstract class IntegrationTest
{
    val apiRequestHelper = APIRequestHelper()
    val webRequestHelper = WebRequestHelper()

    val JSONValidator = JSONValidator()

    companion object
    {
        var appStarted: Boolean = false

        @BeforeAll
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

    @BeforeEach
    fun createDatabase()
    {
        println("Copying database from: ${AppConfig()["db.template"]}")

        val newDbFile = File(AppConfig()["db.location"])
        val source = File(AppConfig()["db.template"])

        source.copyTo(newDbFile, true)
        Thread.sleep(1000)
    }

    @AfterEach
    fun deleteDatabases()
    {
        File(AppConfig()["db.location"]).delete()
    }

    protected fun assertSuccessful(response: Response)
    {
        Assertions.assertThat(response.statusCode)
                .isEqualTo(200)
    }

    protected fun assertSuccessfulWithResponseText(response: Response)
    {
        Assertions.assertThat(response.statusCode)
                .withFailMessage(response.text)
                .isEqualTo(200)
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
        Assertions.assertThat(response.headers["content-type"]).contains("application/json")
    }

    protected fun assertHtmlContentType(response: Response)
    {
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/html")
    }
    protected fun assertPlainTextContentType(response: Response)
    {
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/plain;charset=utf-8")
    }

    protected fun assertWebUrlSecured(url: String,
                                      requiredPermissions: Set<ReifiedPermission>,
                                      contentType: String = ContentTypes.html,
                                      method: HttpMethod = HttpMethod.get,
                                      postData: Map<String, Any>? = null)
    {
        val checker = WebPermissionChecker(url, requiredPermissions, contentType, method, postData)
        checker.checkPermissionsAreSufficient()

        for (permission in requiredPermissions)
        {
            checker.checkPermissionIsRequired(permission)
        }
    }

    protected fun assertAPIUrlSecured(url: String,
                                      requiredPermissions: Set<ReifiedPermission>,
                                      contentType: String = ContentTypes.binarydata,
                                      method: HttpMethod = HttpMethod.get,
                                      postData: Map<String, String>? = null)
    {
        val checker = APIPermissionChecker(url, requiredPermissions, contentType, method, postData)
        checker.checkPermissionsAreSufficient()

        for (permission in requiredPermissions)
        {
            checker.checkPermissionIsRequired(permission)
        }
    }

    protected fun getGitBranchCommit(branch: String): String
    {
        val commits = OrderlyServerClient(AppConfig()).get(
                "/git/commits",
                context = mock {
                    on { queryString() } doReturn "branch=$branch"
                }
        )
        return commits.listData(GitCommit::class.java).first().id
    }
}
