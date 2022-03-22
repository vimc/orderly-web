package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeUserManager
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class UserTests : IntegrationTest()
{
    private val ADDED_USER = "added.user@test.com"

    @Test
    fun `can add user`()
    {
        assertAddedUserExists(false)

        val userEmail = fakeUserManager()
        val body = mapOf(
                "email" to ADDED_USER,
                "username" to "added.user",
                "displayName" to "Added User",
                "source" to "Montagu"
        )
        val response = apiRequestHelper.post("/user/add/", body, userEmail = userEmail)
        assertSuccessful(response)

        assertAddedUserExists(true)
    }

    @Test
    fun `only user managers can add user without user manage permission`()
    {
        val body = mapOf(
                "email" to "not.added.user@test.com",
                "username" to "not.added.user",
                "displayName" to "Not Added User",
                "source" to "Montagu"
        )
        assertAPIUrlSecured("/user/add/",
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                method = HttpMethod.post,
                contentType = ContentTypes.json,
                postData = body)
    }

    private fun assertAddedUserExists(exists: Boolean)
    {
        JooqContext().use {
            val result = it.dsl.select(Tables.ORDERLYWEB_USER.EMAIL)
                    .from(Tables.ORDERLYWEB_USER)
                    .where(Tables.ORDERLYWEB_USER.EMAIL.eq(ADDED_USER))
                    .fetch()
            if (exists)
            {
                Assertions.assertThat(result.count()).isEqualTo(1)
            }
            else
            {
                Assertions.assertThat(result.count()).isEqualTo(0)
            }
        }
    }
}
