package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyUserData
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER_GROUP
import org.vaccineimpact.orderlyweb.models.User

class UserDataTests : DatabaseTests()
{

    @Test
    fun `addUser can create new user`()
    {
        val sut = OrderlyUserData()
        sut.addUser("user.name", "email@somewhere.com")

        val result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.USERNAME.eq("user.name"))
                    .fetchOneInto(User::class.java)
        }

        assertThat(result.email).isEqualTo("email@somewhere.com")
    }

    @Test
    fun `addUser adds user group`()
    {
        val sut = OrderlyUserData()
        sut.addUser("user.name", "email@somewhere.com")
        sut.addUser("user.name", "anotheremail@somewhere.com")

        val result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP)
                    .where(ORDERLYWEB_USER_GROUP.ID.eq("user.name"))
                    .fetchOne()
        }

        assertThat(result).isEqualTo("user.name")
    }

    @Test
    fun `addUser does nothing if user exists`()
    {
        val sut = OrderlyUserData()
        sut.addUser("user.name", "email@somewhere.com")
        sut.addUser("user.name", "anotheremail@somewhere.com")

        val result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.USERNAME.eq("user.name"))
                    .fetchOneInto(User::class.java)
        }

        assertThat(result.email).isEqualTo("email@somewhere.com")
    }
}