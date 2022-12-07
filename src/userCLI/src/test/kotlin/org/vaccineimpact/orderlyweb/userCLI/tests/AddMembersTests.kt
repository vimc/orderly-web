package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER_GROUP_USER
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.userCLI.*

class AddMembersTests : CleanDatabaseTests()
{
    @Test
    fun `addMembers adds members to group`()
    {
        addUserGroups(mapOf("<name>" to listOf("[admin]")))
        addUsers(mapOf("<email>" to listOf("[a.user@email.com]")))

        val result = addMembers(mapOf("<group>" to "[admin]", "<email>" to listOf("[a.user@email.com]")))
        val members = JooqContext().use {

            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP_USER)
                    .where(ORDERLYWEB_USER_GROUP_USER.USER_GROUP.eq("admin"))
                    .fetchOne()
        }

        assertThat(result).isEqualTo("Added user with email 'a.user@email.com' to user group 'admin'")
        assertThat(members[ORDERLYWEB_USER_GROUP_USER.EMAIL]).isEqualTo("a.user@email.com")
        assertThat(members[ORDERLYWEB_USER_GROUP_USER.USER_GROUP]).isEqualTo("admin")
    }
}