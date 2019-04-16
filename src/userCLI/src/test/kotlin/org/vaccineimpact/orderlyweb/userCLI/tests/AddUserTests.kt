package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.userCLI.addUser

class AddUserTests : CleanDatabaseTests()
{
    @Test
    fun `addUser adds user`()
    {
        val result = addUser(mapOf("<email>" to "[test.user@email.com]"))

        val users = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER).fetch()
        }

        Assertions.assertThat(result).isEqualTo("Saved user with email 'test.user@email.com' to the database")
        Assertions.assertThat(users.count()).isEqualTo(1)
    }

    @Test
    fun `addUser does nothing if user exists`()
    {
        var result = addUser(mapOf("<email>" to "[test.user@email.com]"))
        Assertions.assertThat(result).isEqualTo("Saved user with email 'test.user@email.com' to the database")

        result = addUser(mapOf("<email>" to "[test.user@email.com]"))
        Assertions.assertThat(result).isEqualTo("User with email 'test.user@email.com' already exists; no changes made")

        val users = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER).fetch()
        }

        Assertions.assertThat(users.count()).isEqualTo(1)
    }
}