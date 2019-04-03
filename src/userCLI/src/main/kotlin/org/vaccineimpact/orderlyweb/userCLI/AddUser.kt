package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.UserSource
import kotlin.system.exitProcess

class AddUser(val exit: (code: Int) -> String = ::exitProcess,
              val question: Question = CommandLineQuestion("Email address")
)
{
    fun execute(args: List<String>)
    {
        val userEmail = getEmailFromArgs(args)

        try
        {
            if (!userExists(userEmail))
            {
                OrderlyUserRepository().addUser(userEmail, "unknown", "unknown", UserSource.CLI)
                println("Saved user with email '$userEmail' to the database")
            }
            else
            {
                println("User with email '$userEmail' already exists; no changes made")
            }
        }
        catch (e: Exception)
        {
            println("An error occurred saving the user to the database:")
            println(e)
        }
    }

    private fun userExists(email: String): Boolean
    {
        JooqContext().use {
            return it.dsl.fetchOne(Tables.ORDERLYWEB_USER, Tables.ORDERLYWEB_USER.EMAIL.eq(email)) != null
        }
    }

    fun getEmailFromArgs(args: List<String>): String
    {
        return when (args.size)
        {
            0 -> question.ask()
            1 -> args[0]
            else ->
            {
                println("Usage: ./user.sh add [EMAIL]")
                println("Leave off all arguments to add user interactively")
                exit(0)
            }
        }
    }
}

