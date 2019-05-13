package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.WebRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.*
import org.vaccineimpact.orderlyweb.tests.integration_tests.validators.JSONValidator
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

    protected fun assertWebUrlSecured(url: String,
        globalPermissionName: String,
        expectedFailureCode: Int = 403)
    {
        //Check that a GET endpoint returns expected failture code when access is attempted by user without required global permission,
        // and 200 response when access is attempted by user with permission

        //The test user may already have this permission - revoke it if so
        val testEmail = "test.user@example.com"
        val authRepo = OrderlyAuthorizationRepository()
        val userPerms = authRepo.getPermissionsForUser(testEmail)
        val hasPerm = userPerms.any{it.scope is Scope.Global && it.name == globalPermissionName}

        if (hasPerm)
        {
            JooqContext().use {


                val permissionID = it.dsl.select(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID)
                                        .from(Tables.ORDERLYWEB_USER_GROUP_PERMISSION)
                                        .join(Tables.ORDERLYWEB_PERMISSION)
                                        .on(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION.eq(Tables.ORDERLYWEB_PERMISSION.ID))
                                        .join(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                                        .on(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID.eq(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID))
                                        .where(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(testEmail))
                                        .and(Tables.ORDERLYWEB_PERMISSION.ID.eq(globalPermissionName))
                                        .fetchAny(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID)

                it.dsl.deleteFrom(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                        .where(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID.eq(permissionID))
                        .execute()

                it.dsl.deleteFrom(Tables.ORDERLYWEB_USER_GROUP_PERMISSION)
                        .where(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID.eq(permissionID))
                        .execute()
            }
        }

        val sessionCookie = webRequestHelper.webLoginWithMontagu()

        var response = webRequestHelper.getWithSessionCookie(url, sessionCookie)

        Assertions.assertThat(response.statusCode).isEqualTo(expectedFailureCode)

        //Add the permission and try again - should succeed this time
        val permission = ReifiedPermission(globalPermissionName, Scope.Global())
        authRepo.ensureUserGroupHasPermission(testEmail, permission)

        response = webRequestHelper.getWithSessionCookie(url, sessionCookie)

        Assertions.assertThat(response.statusCode).isEqualTo(200)
    }
}