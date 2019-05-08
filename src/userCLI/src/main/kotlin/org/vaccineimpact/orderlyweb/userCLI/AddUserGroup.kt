package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

fun addUserGroups(options: Map<String, Any>): String
{
    val groupNames = options["<name>"] as List<*>

    return groupNames.joinToString("\n") {
        val groupName = it.getStringValue()
        OrderlyAuthorizationRepository().createUserGroup(groupName)
        "Saved user group '$groupName' to the database"
    }
}
