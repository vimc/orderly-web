package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository

fun addMembers(options: Map<String, Any>)
{
    val group = options["<group>"].toString()
    val emails = options["<email>"] as List<*>
    try
    {
        val authRepo = OrderlyAuthorizationRepository()
        emails.map {
            val email = it.toString()

            authRepo.ensureGroupHasMember(group, email)
            println("Added user with email '$email' to user group '$group'")
        }
    }
    catch (e: Exception)
    {
        println("An error occurred saving the membership to the database:")
        println(e)
    }
}

