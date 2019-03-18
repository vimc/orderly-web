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
    fun `addGithubUser can create new github user`()
    {
        val sut = OrderlyUserData()
        sut.addGithubUser("user.name", "email@somewhere.com")

        val result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.USERNAME.eq("user.name"))
                    .fetchOneInto(User::class.java)
        }

        assertThat(result.email).isEqualTo("email@somewhere.com")
        assertThat(result.source).isEqualTo("github")
    }

    @Test
    fun `addGithubUser adds user group`()
    {
        val sut = OrderlyUserData()
        sut.addGithubUser("user.name", "email@somewhere.com")

        val result = JooqContext().use {
            it.dsl.select(ORDERLYWEB_USER_GROUP.ID)
                    .from(ORDERLYWEB_USER_GROUP)
                    .fetchOneInto(String::class.java)
        }

        assertThat(result).isEqualTo("email@somewhere.com")
    }

    @Test
    fun `addGithubUser does nothing if email already exists`()
    {
        val sut = OrderlyUserData()
        sut.addGithubUser("user.name", "email@somewhere.com")
        sut.addGithubUser("another name", "email@somewhere.com")

        val result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.EMAIL.eq("email@somewhere.com"))
                    .fetchOneInto(User::class.java)
        }

        assertThat(result.username).isEqualTo("user.name")
    }
}