package org.vaccineimpact.orderlyweb.userCLI.tests

import org.junit.Test
import org.vaccineimpact.orderlyweb.userCLI.NewUser
import org.vaccineimpact.orderlyweb.userCLI.addUser

class AddUserTests()
{
    @Test
    fun `can parse command line args without if-not-exists flag`()
    {
        val result = NewUser.fromArgs(listOf("test.user@email.com"))
        assert(result.alwaysCreate)
        assert(result.email == "test.user@email.com")
    }

    @Test
    fun `can parse command line args with if-not-exists flag`()
    {
        val result = NewUser.fromArgs(listOf("test.user@email.com", "--if-not-exists"))
        assert(!result.alwaysCreate)
        assert(result.email == "test.user@email.com")
    }


    @Test
    fun `addUser fails if more than 2 args`()
    {
        val result = addUser(listOf("test.user@email.com", "more info", ""))

    }
}