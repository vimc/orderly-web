package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER_GROUP
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.userCLI.addUserGroup

class AddUserGroupTests : CleanDatabaseTests()
{
    @Test
    fun `addUser adds user`()
    {
        val result = addUserGroup(mapOf("<name>" to "funders"))

        val groups = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP).fetch()
        }

        assertThat(result).isEqualTo("Saved user group 'funders' to the database")
        assertThat(groups.count()).isEqualTo(1)
        assertThat(groups.first()[ORDERLYWEB_USER_GROUP.ID]).isEqualTo("funders")
    }

    @Test
    fun `addUser does nothing if group exists`()
    {
        addUserGroup(mapOf("<name>" to "funders"))

        assertThatThrownBy {
            addUserGroup(mapOf("<name>" to "funders"))
        }.hasMessageContaining("An object with the id 'funders' already exists")

        val groups = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP).fetch()
        }

        assertThat(groups.count()).isEqualTo(1)
    }
}