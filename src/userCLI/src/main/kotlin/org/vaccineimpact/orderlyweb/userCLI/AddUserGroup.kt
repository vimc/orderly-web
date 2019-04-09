package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.UserSource

fun addUserGroup(options: Map<String, Any>)
{
    val groupName = options["<name>"].toString()
    try
    {
        if (!groupExists(groupName))
        {
            OrderlyAuthorizationRepository().createUserGroup(groupName)
            println("Saved user group '$groupName' to the database")
        }
        else
        {
            println("User group '$groupName' already exists; no changes made")
        }
    }
    catch (e: Exception)
    {
        println("An error occurred saving the user group to the database:")
        println(e)
    }
}

private fun groupExists(name: String): Boolean
{
    JooqContext().use {
        return it.dsl.fetchOne(Tables.ORDERLYWEB_USER_GROUP, Tables.ORDERLYWEB_USER_GROUP.ID.eq(name)) != null
    }
}


