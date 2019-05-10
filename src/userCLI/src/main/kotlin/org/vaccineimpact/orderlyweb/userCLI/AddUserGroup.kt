package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.errors.DuplicateKeyError

fun addUserGroups(options: Map<String, Any>): String
{
    val groupNames = options["<name>"] as List<*>

    return groupNames.joinToString("\n") {
        val groupName = it.getStringValue()
        try
        {
            OrderlyAuthorizationRepository().createUserGroup(groupName)
            "Saved user group '$groupName' to the database"
        }
        catch(e: DuplicateKeyError) {
            "User group '$groupName' already exists; no changes made"
        }
    }
}
