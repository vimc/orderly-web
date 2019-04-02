package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.UserSource
import kotlin.system.exitProcess

data class NewUser(
        val email: String,
        val alwaysCreate: Boolean = true
)
{
    companion object
    {
        fun fromArgs(args: List<String>): NewUser
        {
            val conditionalCreate = args.size == 2 && args[1] == "--if-not-exists"
            return NewUser(args[0], !conditionalCreate)
        }
    }
}

fun addUser(args: List<String>)
{
    val user = when (args.size)
    {
        0 -> NewUser(Question("Email address").ask(), true)
        1, 2 -> NewUser.fromArgs(args)
        else ->
        {
            println("Usage: ./user.sh add [EMAIL [--if-not-exists]]")
            println("Leave off all arguments to add user interactively")
            exitProcess(0)
        }
    }

    try
    {
        if (user.alwaysCreate || !userExists(user.email))
        {
            OrderlyUserRepository().addUser(user.email, "unknown", "unknown", UserSource.CLI)
            println("Saved user with email '${user.email}' to the database")
        }
        else
        {
            println("User with email '${user.email}' already exists; no changes made")
        }
    }
    catch (e: Exception)
    {
        println("An error occurred saving the user to the database:")
        println(e)
    }
}

fun userExists(email: String): Boolean
{
    JooqContext().use {
        return it.dsl.fetchOne(Tables.ORDERLYWEB_USER, Tables.ORDERLYWEB_USER.EMAIL.eq(email)) != null
    }
}