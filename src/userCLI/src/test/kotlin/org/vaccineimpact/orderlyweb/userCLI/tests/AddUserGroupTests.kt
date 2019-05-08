package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER_GROUP
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.userCLI.addUserGroups

class AddUserGroupTests : CleanDatabaseTests()
{
    @Test
    fun `addUserGroups adds groups`()
    {
        val result = addUserGroups(mapOf("<name>" to listOf("[funders]", "[admin]")))

        val groups = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP).fetch()
        }

        assertThat(result).isEqualTo("""Saved user group 'funders' to the database
            |Saved user group 'admin' to the database
        """.trimMargin())
        assertThat(groups.count()).isEqualTo(2)
        assertThat(groups.first()[ORDERLYWEB_USER_GROUP.ID]).isEqualTo("funders")
        assertThat(groups.last()[ORDERLYWEB_USER_GROUP.ID]).isEqualTo("admin")
    }

    @Test
    fun `addUserGroups does nothing if group exists`()
    {
        addUserGroups(mapOf("<name>" to listOf("[funders]")))

        assertThatThrownBy {
            addUserGroups(mapOf("<name>" to listOf("[funders]")))
        }.hasMessageContaining("An object with the id 'funders' already exists")

        val groups = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP).fetch()
        }

        assertThat(groups.count()).isEqualTo(1)
    }
}