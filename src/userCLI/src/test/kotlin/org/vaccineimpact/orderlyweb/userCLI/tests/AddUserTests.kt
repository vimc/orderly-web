package org.vaccineimpact.orderlyweb.userCLI.tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.userCLI.AddUser
import org.vaccineimpact.orderlyweb.userCLI.Question

class AddUserTests : CleanDatabaseTests()
{
    private fun mockExit(code: Int): String
    {
        return "exited with code $code"
    }

    @Test
    fun `addUser adds user`()
    {
        val sut = AddUser()
        sut.execute(listOf("test.user@email.com"))

        val users = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER).fetch()
        }

        Assertions.assertThat(users.count()).isEqualTo(1)
    }

    @Test
    fun `addUser does nothing if user exists`()
    {
        val sut = AddUser()
        sut.execute(listOf("test.user@email.com"))
        sut.execute(listOf("test.user@email.com"))

        val users = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER).fetch()
        }

        Assertions.assertThat(users.count()).isEqualTo(1)
    }

    @Test
    fun `addUser exits if more than 1 args`()
    {
        val sut = AddUser(::mockExit)
        val result = sut.getEmailFromArgs(listOf("test.user@email.com", "more info"))
        assertThat(result).isEqualTo("exited with code 0")
    }

    @Test
    fun `getEmailFromArgs asks question if 0 args`()
    {
        val mockQuestion = mock<Question> {
            on { it.ask() } doReturn "answer"
        }
        val sut = AddUser(question = mockQuestion)
        val result = sut.getEmailFromArgs(listOf())
        assertThat(result).isEqualTo("answer")
    }
}