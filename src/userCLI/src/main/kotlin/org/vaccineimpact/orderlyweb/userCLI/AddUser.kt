package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.UserSource

fun addUsers(options: Map<String, Any>): String
{
    val userEmails = options["<email>"] as List<*>
    val userRepo = OrderlyUserRepository()

    return userEmails.joinToString("\n") {
        val email = it.getStringValue()
        if (userRepo.getUser(email) == null)
        {
            userRepo.addUser(email, "unknown", "unknown", UserSource.CLI)
            "Saved user with email '$email' to the database"
        }
        else
        {
            "User with email '$email' already exists; no changes made"
        }
    }
}
