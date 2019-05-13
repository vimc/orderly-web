package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.userCLI.addUsers

class AddUserTests : CleanDatabaseTests()
{
    @Test
    fun `addUser adds user`()
    {
        val result = addUsers(mapOf("<email>" to listOf("[test.user@email.com]")))

        val users = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER).fetch()
        }

        Assertions.assertThat(result).isEqualTo("Saved user with email 'test.user@email.com' to the database")
        Assertions.assertThat(users.count()).isEqualTo(1)
    }

    @Test
    fun `addUser does nothing if user exists`()
    {
        val result = addUsers(mapOf("<email>" to listOf("[test.user@email.com]", "[test.user@email.com]")))
        Assertions.assertThat(result).isEqualTo("""Saved user with email 'test.user@email.com' to the database
            |User with email 'test.user@email.com' already exists; no changes made""".trimMargin())

        val users = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER).fetch()
        }

        Assertions.assertThat(users.count()).isEqualTo(1)
    }
}