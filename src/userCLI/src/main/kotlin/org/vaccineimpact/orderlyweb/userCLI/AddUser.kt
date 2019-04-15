package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.UserSource

fun addUser(options: Map<String, Any>): String
{
    val userEmail = options["<email>"].getStringValue()
    val userRepo = OrderlyUserRepository()

    return if (userRepo.getUser(userEmail) == null)
    {
        userRepo.addUser(userEmail, "unknown", "unknown", UserSource.CLI)
        "Saved user with email '$userEmail' to the database"
    }
    else
    {
        "User with email '$userEmail' already exists; no changes made"
    }
}
