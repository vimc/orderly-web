package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import java.time.Instant

class UserRepositoryTests : CleanDatabaseTests()
{

    @Test
    fun `addUser can create new github user`()
    {
        val then = Instant.now()
        val sut = OrderlyUserRepository()
        sut.addUser("email@somewhere.com", "user.name", "full name", UserSource.GitHub)

        val result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.USERNAME.eq("user.name"))
                    .fetchOneInto(User::class.java)
        }

        assertThat(result.email).isEqualTo("email@somewhere.com")
        assertThat(result.source).isEqualTo("GitHub")
        assertThat(result.displayName).isEqualTo("full name")
        assertThat(result.lastLoggedIn).isBetween(then, Instant.now())
    }

    @Test
    fun `addUser adds user group and adds user to group`()
    {
        val email = "email@somewhere.com"
        val sut = OrderlyUserRepository()
        sut.addUser(email,"user.name", "full name", UserSource.GitHub)

        val result = JooqContext().use {
            it.dsl.select(ORDERLYWEB_USER_GROUP.ID)
                    .from(ORDERLYWEB_USER_GROUP)
                    .fetchOneInto(String::class.java)
        }

        assertThat(result).isEqualTo(email)

        val userResult = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP_USER)
                    .fetchOne()
        }

        assertThat(userResult[ORDERLYWEB_USER_GROUP_USER.EMAIL]).isEqualTo(email)
        assertThat(userResult[ORDERLYWEB_USER_GROUP_USER.USER_GROUP]).isEqualTo(email)
    }

    @Test
    fun `addUser updates source, username and last logged in, if email already exists`()
    {
        val sut = OrderlyUserRepository()
        sut.addUser("email@somewhere.com", "user.name", "full name", UserSource.Montagu)

        var result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.EMAIL.eq("email@somewhere.com"))
                    .fetchOneInto(User::class.java)
        }

        assertThat(result.username).isEqualTo("user.name")
        assertThat(result.source).isEqualTo("Montagu")

        val then = Instant.now()
        sut.addUser("email@somewhere.com", "another.name", "full name", UserSource.GitHub)

        result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.EMAIL.eq("email@somewhere.com"))
                    .fetchOneInto(User::class.java)
        }

        assertThat(result.username).isEqualTo("another.name")
        assertThat(result.source).isEqualTo("GitHub")
        assertThat(result.lastLoggedIn).isBetween(then, Instant.now())
    }
}