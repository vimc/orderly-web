package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

fun addMembers(options: Map<String, Any>)
{
    val group = options["<group>"].toString()
    val emails = options["<email>"] as List<*>
    try
    {
        emails.map {
            val email = it.toString()
            if (!userExists(email))
            {
                println("User with email '$email' does not exist; no changes made")
            }
            else
            {
                OrderlyAuthorizationRepository().ensureGroupHasMember(group, email)
                println("Added user with email '$email' to user group '$group'")
            }
        }
    }
    catch (e: Exception)
    {
        println("An error occurred saving the membership to the database:")
        println(e)
    }
}

