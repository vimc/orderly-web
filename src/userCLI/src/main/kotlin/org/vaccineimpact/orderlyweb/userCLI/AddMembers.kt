package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository

fun addMembers(options: Map<String, Any>): String
{
    val group = options["<group>"].toString()
    val emails = options["<email>"] as List<*>

    val authRepo = OrderlyAuthorizationRepository()
    return emails.joinToString("\n") {
        val email = it.toString()

        authRepo.ensureGroupHasMember(group, email)
        "Added user with email '$email' to user group '$group'"
    }
}

