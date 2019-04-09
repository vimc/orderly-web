package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.UserSource

fun addUser(options: Map<String, Any>): String
{
    val userEmail = options["<email>"].toString()
    val userRepo = OrderlyUserRepository()

    return try
    {
        if (userRepo.getUser(userEmail) == null)
        {
            userRepo.addUser(userEmail, "unknown", "unknown", UserSource.CLI)
            "Saved user with email '$userEmail' to the database"
        }
        else
        {
            "User with email '$userEmail' already exists; no changes made"
        }
    }
    catch (e: Exception)
    {
        "An error occurred saving the user to the database:\n $e"
    }
}
