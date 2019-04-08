package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.UserSource

fun addUser(options: Map<String, Any>)
{
    val userEmail = options["<email>"].toString()
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


