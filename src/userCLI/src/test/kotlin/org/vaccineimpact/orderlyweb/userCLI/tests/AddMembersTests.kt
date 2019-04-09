package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER_GROUP_USER
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.userCLI.addMembers
import org.vaccineimpact.orderlyweb.userCLI.addUser
import org.vaccineimpact.orderlyweb.userCLI.addUserGroup

class AddMembersTests : CleanDatabaseTests()
{
    @Test
    fun `addMembers adds members to group`()
    {
        addUserGroup(mapOf("<name>" to "admin"))
        addUser(mapOf("<email>" to "a.user@email.com"))
        addMembers(mapOf("<group>" to "admin", "<email>" to listOf("a.user@email.com")))
        val members = JooqContext().use {

            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP_USER)
                    .where(ORDERLYWEB_USER_GROUP_USER.USER_GROUP.eq("admin"))
                    .fetchOne()
        }

        Assertions.assertThat(members[ORDERLYWEB_USER_GROUP_USER.EMAIL]).isEqualTo("a.user@email.com")
        Assertions.assertThat(members[ORDERLYWEB_USER_GROUP_USER.USER_GROUP]).isEqualTo("admin")
    }
}