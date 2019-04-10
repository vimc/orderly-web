package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

fun addUserGroup(options: Map<String, Any>): String
{
    val groupName = options["<name>"].toString()

    OrderlyAuthorizationRepository().createUserGroup(groupName)
    return "Saved user group '$groupName' to the database"
}
